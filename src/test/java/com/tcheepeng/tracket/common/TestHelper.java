package com.tcheepeng.tracket.common;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.model.AssetClass;
import com.tcheepeng.tracket.stock.model.Stock;
import com.tcheepeng.tracket.stock.model.Trade;
import com.tcheepeng.tracket.stock.model.TradeType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class TestHelper {

  public static List<Trade> getAllTradesForTestAccount() {
    Trade account1Buy = new Trade();
    Trade account1Buy2 = new Trade();
    Trade account1Sell = new Trade();
    Trade account1Div = new Trade();

    account1Buy.setId(0);
    account1Buy.setAccount(1);
    account1Buy.setTradeTs(Timestamp.from(Instant.ofEpochMilli(1000)));
    account1Buy.setTradeType(TradeType.BUY);
    account1Buy.setNumOfUnits(5);
    account1Buy.setPricePerUnit(107);
    account1Buy.setName("A35");
    account1Buy.setFee(100);

    account1Buy2.setId(1);
    account1Buy2.setAccount(1);
    account1Buy2.setTradeTs(Timestamp.from(Instant.ofEpochMilli(1001)));
    account1Buy2.setTradeType(TradeType.BUY);
    account1Buy2.setNumOfUnits(10);
    account1Buy2.setPricePerUnit(326);
    account1Buy2.setName("ES3");
    account1Buy2.setFee(102);

    account1Sell.setId(2);
    account1Sell.setAccount(1);
    account1Sell.setTradeTs(Timestamp.from(Instant.ofEpochMilli(1002)));
    account1Sell.setTradeType(TradeType.SELL);
    account1Sell.setNumOfUnits(2);
    account1Sell.setPricePerUnit(105);
    account1Sell.setName("A35");
    account1Sell.setFee(90);
    account1Sell.setBuyId(0);

    account1Div.setId(3);
    account1Div.setAccount(1);
    account1Div.setTradeTs(Timestamp.from(Instant.ofEpochMilli(1000)));
    account1Div.setTradeType(TradeType.DIVIDEND);
    account1Div.setNumOfUnits(10);
    account1Div.setPricePerUnit(80);
    account1Div.setName("ES3");
    account1Div.setFee(0);

    return List.of(account1Buy, account1Buy2, account1Sell, account1Div);
  }

  public static AccountResponse getTestAccountResponse() {
    return AccountResponse.builder()
        .id(1)
        .name("Test Account")
        .currency("SGD")
        .accountType(AccountType.INVESTMENT)
        .description("Account description")
        .cashInCents(1000)
        .assetValueInCents(1000)
        .build();
  }

  public static Account getTestAccount() {
    Account account = new Account();
    account.setId(1);
    account.setName("Test Account");
    account.setCurrency("SGD");
    account.setCreationTs(Timestamp.from(Instant.ofEpochMilli(1000000)));
    account.setAccountType(AccountType.INVESTMENT);
    account.setDescription("Account description");
    account.setCashInCents(1000);
    account.setDeleted(false);
    return account;
  }

  public static Stock getTestStock() {
    Stock stock = new Stock();
    stock.setName("ABC");
    stock.setCurrency("SGD");
    stock.setAssetClass(AssetClass.BOND);
    stock.setDeleted(false);
    stock.setDisplayTickerSymbol("Display");
    return stock;
  }

  public static CreateAccountRequest getCreateAccountRequest() {
    return CreateAccountRequest.builder()
        .accountType(AccountType.INVESTMENT)
        .name("Test Account")
        .currency("SGD")
        .description("Account description")
        .cashInCents(1000)
        .build();
  }

  public static PatchAccountRequest getPatchAccountRequest() {
    return PatchAccountRequest.builder()
        .id(1)
        .name("Test change name")
        .description("Test change desc")
        .build();
  }

  public static CreateStockRequest getCreateStockRequest() {
    return CreateStockRequest.builder()
        .name("ABC")
        .currency("SGD")
        .assetClass(AssetClass.BOND)
        .displayTickerSymbol("ABC.SI")
        .apiTickers(Map.of(ApiStrategy.YAHOO_FINANCE, "ABC.SI", ApiStrategy.RAW_URL, "ABC.RAW"))
        .build();
  }

  public static PatchStockRequest getPatchStockRequest() {
    return PatchStockRequest.builder()
        .name("ABC")
        .currency("SGD")
        .assetClass(AssetClass.EQUITY)
        .displayTickerSymbol("Display")
        .build();
  }

  public static TradeStockRequest getTradeStockRequest() {
    return TradeStockRequest.builder()
        .timestamp(1L)
        .tradeType(TradeType.BUY)
        .numOfUnits(1)
        .pricePerUnitInMilli(1000)
        .name("ABC")
        .accountId(0)
        .feeInMilli(1000)
        .buyId(null)
        .build();
  }

  public static void assertInternalServerError(String jsonReply) throws JSONException {
    String expectedJson =
        "{\"status\":\"FAIL\",\"errors\":[{\"code\":\"generic\",\"message\":\"Internal server error\"}],\"data\":null}";
    assertEquals(jsonReply, expectedJson, JSONCompareMode.STRICT);
  }

  public static AccountGroup getTestAccountGroup() {
    AccountGroup accountGroup = new AccountGroup();
    accountGroup.setId(1);
    accountGroup.setName("ABC");
    accountGroup.setCurrency("SGD");
    return accountGroup;
  }

  public static AccountAccountGroup getTestAccountAccountGroup() {
    AccountAccountGroup accountGroup = new AccountAccountGroup();
    AccountAccountGroup.EmbeddedAccountAccountGroup embeddedAccountAccountGroup =
        new AccountAccountGroup.EmbeddedAccountAccountGroup();
    embeddedAccountAccountGroup.setAccountId(1);
    embeddedAccountAccountGroup.setAccountGroupId(1);
    accountGroup.setAccountAccountGroup(embeddedAccountAccountGroup);
    return accountGroup;
  }

  public static AccountTransactionRequest getDepositRequest() {
    return AccountTransactionRequest.builder()
        .accountIdFrom(0)
        .amountsInCents(1000)
        .transactionType(AccountTransactionType.DEPOSIT)
        .build();
  }

  public static AccountTransactionRequest getWithdrawRequest() {
    return AccountTransactionRequest.builder()
        .accountIdFrom(0)
        .amountsInCents(1000)
        .transactionType(AccountTransactionType.WITHDRAW)
        .build();
  }

  public static AccountTransactionRequest getTransferRequest() {
    return AccountTransactionRequest.builder()
        .accountIdFrom(0)
        .accountIdTo(1)
        .amountsInCents(254)
        .transactionType(AccountTransactionType.TRANSFER)
        .exchangeRateInMilli(74560)
        .build();
  }
}
