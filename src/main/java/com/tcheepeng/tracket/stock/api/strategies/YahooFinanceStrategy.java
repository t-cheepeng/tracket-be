package com.tcheepeng.tracket.stock.api.strategies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcheepeng.tracket.stock.api.ApiStrategy;
import com.tcheepeng.tracket.stock.api.ExternalApiResponse;
import com.tcheepeng.tracket.stock.api.ExternalSearchResponse;
import com.tcheepeng.tracket.stock.api.StockApiException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Slf4j
public class YahooFinanceStrategy implements BaseStrategy {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private String ticker;
  private boolean isDaily = false;

  private YahooFinanceStrategy() {
    this.httpClient = new OkHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  public static BaseStrategy init() {
    return new YahooFinanceStrategy();
  }

  @Override
  public BaseStrategy withTicker(String ticker) {
    this.ticker = ticker;
    return this;
  }

  @Override
  public BaseStrategy daily() {
    isDaily = true;
    return this;
  }

  @Override
  public ExternalApiResponse fetch() throws StockApiException {
    if (ticker == null) {
      throw new StockApiException("Stock ticker cannot be null");
    }

    try {
      Stock stock = YahooFinance.get(ticker);
      String name = stock.getName();
      BigDecimal price = stock.getQuote().getPrice();
      String currency = stock.getCurrency();
      return ExternalApiResponse.builder()
          .stockName(name)
          .priceInMill(price.multiply(BigDecimal.valueOf(1000)).longValue())
          .currency(Currency.getInstance(currency))
          .build();
    } catch (IOException e) {
      throw new StockApiException("Unable to find stocker ticker: " + ticker);
    } catch (Exception e) {
      throw new StockApiException(e);
    }
  }

  @Override
  public List<ExternalSearchResponse> search(String query) throws StockApiException {
    String searchUrl =
        UriComponentsBuilder.fromUriString("https://query2.finance.yahoo.com/v1/finance/search")
            .queryParam("q", UriUtils.encodePathSegment(query, StandardCharsets.UTF_8))
            .queryParam("newsCount", "0")
            .queryParam("listsCount", "0")
            .queryParam("quotesCount", "6")
            .build()
            .toString();
    log.info("Querying yahoo finance search API {}", searchUrl);
    Request request = new Request.Builder().url(searchUrl).build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.info("Unable to query yahooo finance API for {}. Response was {}", query, response);
      }

      if (response.body() == null) {
        return List.of();
      }

      JsonNode responseTree = objectMapper.readTree(response.body().string());
      if (!responseTree.has("quotes")) {
        log.info(
            "Unable to query yahoo finance API for {}. No quotes. Response was {}",
            query,
            responseTree.toPrettyString());
      }

      JsonNode quotes = responseTree.get("quotes");
      if (!quotes.isArray()) {
        log.info(
            "Unable to query yahoo finance API for {}. Quotes object not an array. Response was {}",
            query,
            responseTree.toPrettyString());
      }

      List<ExternalSearchResponse> result = new ArrayList<>(12);
      for (final JsonNode quote : quotes) {
        log.debug("Running through yahoo finance qoutes: {}", quote);
        ExternalSearchResponse.ExternalSearchResponseBuilder builder =
            ExternalSearchResponse.builder().apiUsed(ApiStrategy.YAHOO_FINANCE);

        if (quote.hasNonNull("longname")) {
          builder = builder.stockName(quote.get("longname").asText(""));
        }

        if (quote.hasNonNull("exchange")) {
          builder = builder.exchange(quote.get("exchange").asText(""));
        }

        if (quote.hasNonNull("symbol")) {
          builder = builder.ticker(quote.get("symbol").asText(""));
        }

        if (quote.hasNonNull("typeDisp")) {
          builder = builder.stockClass(quote.get("typeDisp").asText(""));
        }

        if (quote.hasNonNull("exchDisp")) {
          builder = builder.exchangeCountry(quote.get("exchDisp").asText(""));
        }

        if (quote.hasNonNull(("score"))) {
          builder = builder.searchScore(quote.get("score").asDouble(0));
        }

        builder = builder.currencyGuess(guessCurrency(quote));

        result.add(builder.build());
      }

      return result;
    } catch (IOException e) {
      log.error("Unable to fetch yahoo search api: ", e);
      throw new StockApiException(e);
    }
  }

  private Currency guessCurrency(JsonNode quote) {
    Currency exchDispGuess = null;
    Currency exchangeGuess = null;
    Currency tickerSymbolGuess = null;
    if (quote.hasNonNull("exchDisp")) {
      String exchDisp = quote.get("exchDisp").asText();
      exchDispGuess = guessCurrencyFromExchDisp(exchDisp);
    }

    if (quote.hasNonNull("exchange")) {
      String exchange = quote.get("exchange").asText();
      exchangeGuess = guessCurrencyFromExchange(exchange);
    }

    if (quote.hasNonNull("symbol")) {
      String symbol = quote.get("symbol").asText();
      tickerSymbolGuess = guessCurrencyFromTickerSymbol(symbol);
    }

    Map<Currency, Long> count =
        Stream.of(exchDispGuess, exchangeGuess, tickerSymbolGuess)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    return count.entrySet().stream()
        .max((entry1, entry2) -> Math.toIntExact(entry1.getValue() - entry2.getValue()))
        .map(Map.Entry::getKey)
        .orElseGet(() -> Currency.getInstance(Locale.US));
  }

  private Currency guessCurrencyFromExchDisp(String exchDisp) {
    String lowerCasedExchDisp = exchDisp.toLowerCase();

    if (lowerCasedExchDisp.contains("london")) {
      return Currency.getInstance(Locale.UK);
    }

    if (lowerCasedExchDisp.contains("nasdaq") || lowerCasedExchDisp.contains("nysearca")) {
      return Currency.getInstance(Locale.US);
    }

    if (lowerCasedExchDisp.contains("singapore")) {
      return Currency.getInstance(Locale.of("en", "SG"));
    }

    return null;
  }

  private Currency guessCurrencyFromExchange(String exchange) {
    String lowerCasedExchange = exchange.toLowerCase();

    if (lowerCasedExchange.contains("lse")) {
      return Currency.getInstance(Locale.UK);
    }

    if (lowerCasedExchange.contains("pcx") || lowerCasedExchange.contains("ngm")) {
      return Currency.getInstance(Locale.US);
    }

    if (lowerCasedExchange.contains("ses")) {
      return Currency.getInstance(Locale.of("en", "SG"));
    }

    return null;
  }

  private Currency guessCurrencyFromTickerSymbol(String symbol) {
    String lowerCasedSymbol = symbol.toLowerCase();
    String[] splitByDot = lowerCasedSymbol.split("\\.");

    if (splitByDot.length == 1) {
      return Currency.getInstance(Locale.US);
    }

    String yahooCountryCode = splitByDot[1];
    if (yahooCountryCode.equalsIgnoreCase("si")) {
      return Currency.getInstance(Locale.of("en", "SG"));
    }

    if (yahooCountryCode.equalsIgnoreCase("l")) {
      return Currency.getInstance(Locale.UK);
    }

    return null;
  }
}
