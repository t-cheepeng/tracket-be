package com.tcheepeng.tracket.stock.service;

import static com.tcheepeng.tracket.common.Utils.toStandardRepresentation;

import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.common.validation.BusinessValidations;
import com.tcheepeng.tracket.external.api.*;
import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import com.tcheepeng.tracket.external.api.model.ExternalSearchResponse;
import com.tcheepeng.tracket.external.service.FetchService;
import com.tcheepeng.tracket.external.service.InformationProcessorService;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.model.*;
import com.tcheepeng.tracket.stock.repository.StockRepository;
import com.tcheepeng.tracket.stock.repository.TickerApiRepository;
import com.tcheepeng.tracket.stock.repository.TradeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockService {

  private final FetchService fetchService;
  private final InformationProcessorService processorService;

  private final StockRepository stockRepository;
  private final TickerApiRepository tickerApiRepository;
  private final TradeRepository tradeRepository;
  private final AccountRepository accountRepository;
  private final TimeOperator timeOperator;

  public StockService(
      final FetchService fetchService,
      final InformationProcessorService processorService,
      final StockRepository stockRepository,
      final TickerApiRepository tickerApiRepository,
      final TradeRepository tradeRepository,
      final AccountRepository accountRepository,
      final TimeOperator timeOperator) {
    this.fetchService = fetchService;
    this.processorService = processorService;
    this.stockRepository = stockRepository;
    this.tickerApiRepository = tickerApiRepository;
    this.tradeRepository = tradeRepository;
    this.accountRepository = accountRepository;
    this.timeOperator = timeOperator;
  }

  public List<ExternalSearchResponse> queryExternalStockApi(String query) {
    List<ExternalSearchResponse> yahooFinanceQueryResponse =
        ExternalApi.yahooFinance().search(query);
    List<ExternalSearchResponse> alpacaMarketQueryResponse =
        ExternalApi.alpacaMarket().search(query);
    List<ExternalSearchResponse> sgxMarketQueryResponse =
        ExternalApi.sgx().search(fetchService, processorService, query);

    return Stream.of(yahooFinanceQueryResponse, alpacaMarketQueryResponse, sgxMarketQueryResponse)
        .flatMap(Collection::stream)
        .toList();
  }

  public Optional<Stock> getStock(String stockName) {
    return stockRepository.findById(stockName);
  }

  public List<Stock> getAllStocks() {
    return stockRepository.findAll().stream().filter(stock -> !stock.isDeleted()).toList();
  }

  @Transactional
  public void createStock(CreateStockRequest request) {
    Stock stock = new Stock();
    stock.setName(request.getName());
    stock.setCurrency(request.getCurrency());
    stock.setAssetClass(request.getAssetClass());
    stock.setDeleted(false);
    stock.setDisplayTickerSymbol(request.getDisplayTickerSymbol());
    stockRepository.save(stock);

    Map<ApiFetcher, String> tickerToApi = request.getApiTickers();
    tickerApiRepository.saveAll(
        tickerToApi.entrySet().stream()
            .map(
                entry -> {
                  TickerApi tickerApi = new TickerApi();
                  tickerApi.setTickerSymbol(entry.getValue());
                  tickerApi.setApi(entry.getKey());
                  tickerApi.setName(request.getName());
                  return tickerApi;
                })
            .toList());
  }

  @Transactional
  public void updateStock(PatchStockRequest request) {
    Stock existingStock =
        stockRepository
            .findById(request.getName())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Unable to find entity (stock) with name: " + request.getName()));

    if (request.getAssetClass() != null) {
      existingStock.setAssetClass(request.getAssetClass());
    }

    if (request.getCurrency() != null) {
      existingStock.setCurrency(request.getCurrency());
    }

    if (request.getDisplayTickerSymbol() != null) {
      existingStock.setDisplayTickerSymbol(request.getDisplayTickerSymbol());
    }

    stockRepository.save(existingStock);
  }

  @Transactional
  public void deleteStock(String name) {
    stockRepository.softDeleteByName(name);
  }

  @Transactional
  public void tradeStock(TradeStockRequest request) {
    verifyBusinessConstraints(request);

    Trade trade = new Trade();
    trade.setTradeTs(timeOperator.getTimestampFromMilliSinceEpoch(request.getTimestamp()));
    trade.setTradeType(request.getTradeType());
    trade.setNumOfUnits(request.getNumOfUnits());
    trade.setPricePerUnit(toStandardRepresentation(request.getPrice()));
    trade.setName(request.getName());
    trade.setAccount(request.getAccountId());
    trade.setFee(
        request.getFee() == null ? BigDecimal.ZERO : toStandardRepresentation(request.getFee()));
    trade.setBuyId(
        request.getTradeType() == TradeType.BUY || request.getTradeType() == TradeType.DIVIDEND
            ? null
            : request.getBuyId());
    tradeRepository.save(trade);
  }

  public List<Trade> getAllTrades() {
    return tradeRepository.findAll();
  }

  private void verifyBusinessConstraints(TradeStockRequest request)
      throws DataIntegrityViolationException, EntityNotFoundException {
    Stock stock =
        stockRepository
            .findById(request.getName())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Unable to find entity (stock) with name: " + request.getName()));
    Account account =
        accountRepository
            .findById(request.getAccountId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Unable to find entity (account) with ID: " + request.getAccountId()));

    if (!stock.getCurrency().equalsIgnoreCase(account.getCurrency())) {
      throwDataIntegrityViolation(
          BusinessValidations.BK_ACCOUNT_STOCK_CURRENCY_MUST_BE_SAME, stock, account);
      return;
    }

    if (stock.isDeleted()) {
      throwDataIntegrityViolation(BusinessValidations.BK_STOCK_NOT_DELETED, stock, account);
      return;
    }

    if (account.isDeleted()) {
      throwDataIntegrityViolation(BusinessValidations.BK_ACCOUNT_NOT_DELETED, stock, account);
      return;
    }

    if (request.getTradeType() == TradeType.BUY || request.getTradeType() == TradeType.DIVIDEND) {
      return;
    }

    if (request.getBuyId() == null) {
      throwDataIntegrityViolation(BusinessValidations.BK_SELL_MUST_HAVE_BUY_ID, stock, account);
      return;
    }

    Optional<Trade> optionalBuyTrade = tradeRepository.findById(request.getBuyId());
    if (optionalBuyTrade.isEmpty()) {
      throwDataIntegrityViolation(BusinessValidations.BK_SELL_WHAT_IS_BOUGHT, stock, account);
      return;
    }

    Trade possibleBuyTrade = optionalBuyTrade.get();
    if (possibleBuyTrade.getAccount() != account.getId()
        || !possibleBuyTrade.getName().equals(stock.getName())) {
      throwDataIntegrityViolation(BusinessValidations.BK_SELL_WRONG_STOCK, stock, account);
    }
  }

  private void throwDataIntegrityViolation(
      BusinessValidations validations, Stock stock, Account account)
      throws DataIntegrityViolationException {
    String builder =
        "Violation of "
            + validations.name()
            + ". "
            + "Violated entities: Stock["
            + stock.toString()
            + "] and account["
            + account.toString()
            + "]";
    throw new DataIntegrityViolationException(builder);
  }
}
