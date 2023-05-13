package com.tcheepeng.tracket.common;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.stock.api.ApiStrategy;
import com.tcheepeng.tracket.stock.controller.request.CreateStockRequest;
import com.tcheepeng.tracket.stock.controller.request.PatchStockRequest;
import com.tcheepeng.tracket.stock.controller.request.TradeStockRequest;
import com.tcheepeng.tracket.stock.model.AssetClass;
import com.tcheepeng.tracket.stock.model.Stock;
import com.tcheepeng.tracket.stock.model.TradeType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class TestHelper {

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
}
