package com.tcheepeng.tracket.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.common.validation.BusinessValidations;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.model.*;
import com.tcheepeng.tracket.stock.repository.StockRepository;
import com.tcheepeng.tracket.stock.repository.TickerApiRepository;
import com.tcheepeng.tracket.stock.repository.TradeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

  @InjectMocks private StockService stockService;

  @Mock private StockRepository stockRepository;

  @Mock private TickerApiRepository tickerApiRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private TradeRepository tradeRepository;
  @Mock private TimeOperator timeOperator;

  @Captor private ArgumentCaptor<List<TickerApi>> tickerApiCaptor;

  @Test
  void Get_stock_is_successful() {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(stockRepository.findById(captor.capture())).thenReturn(Optional.empty());

    stockService.getStock("ABC");

    assertThat(captor.getValue()).isEqualTo("ABC");
  }

  @Test
  void Delete_stock_is_successful() {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doNothing().when(stockRepository).softDeleteByName(captor.capture());

    stockService.deleteStock("ABC");

    assertThat(captor.getValue()).isEqualTo("ABC");
  }

  @Test
  void Create_stock_is_successful() {
    ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
    CreateStockRequest request =
        CreateStockRequest.builder()
            .name("ABC")
            .assetClass(AssetClass.BOND)
            .currency("SGD")
            .displayTickerSymbol("Display")
            .apiTickers(Map.of(ApiStrategy.YAHOO_FINANCE, "ABC.SI", ApiStrategy.RAW_URL, "ABC"))
            .build();

    stockService.createStock(request);

    Stock expectedStock = new Stock();
    expectedStock.setName("ABC");
    expectedStock.setCurrency("SGD");
    expectedStock.setAssetClass(AssetClass.BOND);
    expectedStock.setDeleted(false);
    expectedStock.setDisplayTickerSymbol("Display");
    verify(stockRepository).save(captor.capture());
    assertThat(captor.getValue()).isEqualTo(expectedStock);

    verify(tickerApiRepository).saveAll(tickerApiCaptor.capture());
    TickerApi expectedYahooApi = new TickerApi();
    expectedYahooApi.setApi(ApiStrategy.YAHOO_FINANCE);
    expectedYahooApi.setTickerSymbol("ABC.SI");
    expectedYahooApi.setName("ABC");
    TickerApi rawApi = new TickerApi();
    rawApi.setApi(ApiStrategy.RAW_URL);
    rawApi.setTickerSymbol("ABC");
    rawApi.setName("ABC");
    assertThat(tickerApiCaptor.getValue()).containsExactlyInAnyOrder(expectedYahooApi, rawApi);
  }

  @Test
  void Update_stock_is_successful() {
    PatchStockRequest request =
        PatchStockRequest.builder()
            .name("ABC")
            .currency("GBP")
            .assetClass(AssetClass.EQUITY)
            .displayTickerSymbol("ABCGBP")
            .build();

    Stock existingStock = TestHelper.getTestStock();
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(existingStock));

    stockService.updateStock(request);

    Stock expectedStock = new Stock();
    expectedStock.setName("ABC");
    expectedStock.setCurrency("GBP");
    expectedStock.setAssetClass(AssetClass.EQUITY);
    expectedStock.setDeleted(false);
    expectedStock.setDisplayTickerSymbol("ABCGBP");
    ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
    verify(stockRepository).save(captor.capture());
    assertThat(captor.getValue()).isEqualTo(expectedStock);
  }

  @Test
  void Update_stock_not_found() {
    PatchStockRequest request = PatchStockRequest.builder().name("Name not found").build();
    when(stockRepository.findById("Name not found")).thenReturn(Optional.empty());

    assertThatExceptionOfType(EntityNotFoundException.class)
        .isThrownBy(() -> stockService.updateStock(request))
        .withMessageContaining("Unable to find entity (stock) with name: Name not found");
  }

  @Test
  void Trade_stock_stock_not_found() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    when(stockRepository.findById("ABC")).thenReturn(Optional.empty());

    assertThatExceptionOfType(EntityNotFoundException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Unable to find entity (stock) with name: ABC");
  }

  @Test
  void Trade_stock_account_not_found() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    Stock stock = TestHelper.getTestStock();
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.empty());

    assertThatExceptionOfType(EntityNotFoundException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Unable to find entity (account) with ID: 0");
  }

  @Test
  void Trade_stock_stock_account_currency_not_same() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    Stock stock = TestHelper.getTestStock();
    Account account = TestHelper.getTestAccount();
    account.setCurrency(Currency.getInstance("XPD").toString());
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining(
            "Violation of " + BusinessValidations.BK_ACCOUNT_STOCK_CURRENCY_MUST_BE_SAME);
  }

  @Test
  void Trade_stock_stock_is_deleted() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(true);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_STOCK_NOT_DELETED);
  }

  @Test
  void Trade_stock_account_is_deleted() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(true);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_ACCOUNT_NOT_DELETED);
  }

  @Test
  void Trade_stock_sell_trade_no_buy_id() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    request.setTradeType(TradeType.SELL);
    request.setBuyId(null);
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_SELL_MUST_HAVE_BUY_ID);
  }

  @Test
  void Trade_stock_sell_trade_buy_trade_cannot_be_found() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    request.setTradeType(TradeType.SELL);
    request.setBuyId(0);
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    when(tradeRepository.findById(0)).thenReturn(Optional.empty());

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_SELL_WHAT_IS_BOUGHT);
  }

  @Test
  void Trade_stock_sell_trade_refer_to_mismatching_buy_trade_under_diff_account() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    request.setTradeType(TradeType.SELL);
    request.setBuyId(0);
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    Trade trade = new Trade();
    trade.setAccount(100);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    when(tradeRepository.findById(0)).thenReturn(Optional.of(trade));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_SELL_WRONG_STOCK);
  }

  @Test
  void Trade_stock_sell_trade_refer_to_mismatching_buy_trade_with_diff_stock_name() {
    TradeStockRequest request = TestHelper.getTradeStockRequest();
    request.setTradeType(TradeType.SELL);
    request.setBuyId(0);
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    Trade trade = new Trade();
    trade.setAccount(request.getAccountId());
    trade.setName("This stock name does not match");
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    when(tradeRepository.findById(0)).thenReturn(Optional.of(trade));

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> stockService.tradeStock(request))
        .withMessageContaining("Violation of " + BusinessValidations.BK_SELL_WRONG_STOCK);
  }

  @Test
  void Trade_stock_buy_trade_is_successful() {
    TradeStockRequest request =
        TradeStockRequest.builder()
            .timestamp(1L)
            .tradeType(TradeType.BUY)
            .numOfUnits(10)
            .pricePerUnitInMilli(1000)
            .name("ABC")
            .accountId(0)
            .feeInMilli(1000)
            .buyId(0)
            .build();
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(1));
    when(timeOperator.getTimestampFromMilliSinceEpoch(1)).thenReturn(timestamp);

    stockService.tradeStock(request);
    Trade expectedTrade = new Trade();
    expectedTrade.setTradeTs(timestamp);
    expectedTrade.setTradeType(TradeType.BUY);
    expectedTrade.setNumOfUnits(10);
    expectedTrade.setPricePerUnit(1000);
    expectedTrade.setName("ABC");
    expectedTrade.setAccount(0);
    expectedTrade.setFee(1000);
    expectedTrade.setBuyId(null);
    ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
    verify(tradeRepository).save(argumentCaptor.capture());
    verify(tradeRepository, times(0)).findById(any());
    assertThat(argumentCaptor.getValue()).isEqualTo(expectedTrade);
  }

  @Test
  void Trade_stock_dividend_trade_is_successful() {
    TradeStockRequest request =
            TradeStockRequest.builder()
                    .timestamp(1L)
                    .tradeType(TradeType.DIVIDEND)
                    .numOfUnits(10)
                    .pricePerUnitInMilli(1000)
                    .name("ABC")
                    .accountId(0)
                    .feeInMilli(0)
                    .build();
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(1));
    when(timeOperator.getTimestampFromMilliSinceEpoch(1)).thenReturn(timestamp);

    stockService.tradeStock(request);
    Trade expectedTrade = new Trade();
    expectedTrade.setTradeTs(timestamp);
    expectedTrade.setTradeType(TradeType.DIVIDEND);
    expectedTrade.setNumOfUnits(10);
    expectedTrade.setPricePerUnit(1000);
    expectedTrade.setName("ABC");
    expectedTrade.setAccount(0);
    expectedTrade.setFee(0);
    expectedTrade.setBuyId(null);
    ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
    verify(tradeRepository).save(argumentCaptor.capture());
    verify(tradeRepository, times(0)).findById(any());
    assertThat(argumentCaptor.getValue()).isEqualTo(expectedTrade);
  }

  @Test
  void Trade_stock_sell_trade_is_successful() {
    TradeStockRequest request =
            TradeStockRequest.builder()
                    .timestamp(1L)
                    .tradeType(TradeType.SELL)
                    .numOfUnits(10)
                    .pricePerUnitInMilli(1000)
                    .name("ABC")
                    .accountId(0)
                    .feeInMilli(0)
                    .buyId(0)
                    .build();
    Stock stock = TestHelper.getTestStock();
    stock.setCurrency("SGD");
    stock.setDeleted(false);
    Account account = TestHelper.getTestAccount();
    account.setCurrency("SGD");
    account.setDeleted(false);
    Trade buyTrade = new Trade();
    buyTrade.setAccount(account.getId());
    buyTrade.setName(stock.getName());
    buyTrade.setId(0);
    when(stockRepository.findById("ABC")).thenReturn(Optional.of(stock));
    when(accountRepository.findById(0)).thenReturn(Optional.of(account));
    when(tradeRepository.findById(0)).thenReturn(Optional.of(buyTrade));
    Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(1));
    when(timeOperator.getTimestampFromMilliSinceEpoch(1)).thenReturn(timestamp);

    stockService.tradeStock(request);
    Trade expectedTrade = new Trade();
    expectedTrade.setTradeTs(timestamp);
    expectedTrade.setTradeType(TradeType.SELL);
    expectedTrade.setNumOfUnits(10);
    expectedTrade.setPricePerUnit(1000);
    expectedTrade.setName("ABC");
    expectedTrade.setAccount(0);
    expectedTrade.setFee(0);
    expectedTrade.setBuyId(0);
    ArgumentCaptor<Trade> argumentCaptor = ArgumentCaptor.forClass(Trade.class);
    verify(tradeRepository).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue()).isEqualTo(expectedTrade);
  }

  @Test
  void Get_all_stocks_no_deleted_returned() throws Exception {
    Stock testStock1 = TestHelper.getTestStock();
    Stock deletedStock = TestHelper.getTestStock();
    deletedStock.setDeleted(true);
    deletedStock.setName("BCD");
    when(stockRepository.findAll()).thenReturn(List.of(testStock1, deletedStock));

    List<Stock> stocks = stockService.getAllStocks();

    assertThat(stocks).containsExactly(testStock1);
  }
}
