package com.tcheepeng.tracket.account.service;

import static com.tcheepeng.tracket.common.Utils.toStandardRepresentation;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.BK_ACCOUNT_MUST_EXIST;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountActivityPageResponse;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.account.model.StockOwned;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.account.repository.AccountTransactionsRepository;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.stock.controller.response.TradeResponse;
import com.tcheepeng.tracket.stock.model.HistoricalStockPrice;
import com.tcheepeng.tracket.stock.model.Trade;
import com.tcheepeng.tracket.stock.model.TradeType;
import com.tcheepeng.tracket.stock.repository.HistoricalStockPriceRepository;
import com.tcheepeng.tracket.stock.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountService {

  private final AccountRepository accountRepository;
  private final AccountTransactionsRepository transactionsRepository;
  private final TimeOperator timeOperator;
  private final TradeRepository tradeRepository;
  private final HistoricalStockPriceRepository historicalStockPriceRepository;

  public AccountService(
      final AccountRepository accountRepository,
      final AccountTransactionsRepository transactionsRepository,
      final TradeRepository tradeRepository,
      final HistoricalStockPriceRepository historicalStockPriceRepository,
      final TimeOperator timeOperator) {
    this.accountRepository = accountRepository;
    this.transactionsRepository = transactionsRepository;
    this.tradeRepository = tradeRepository;
    this.historicalStockPriceRepository = historicalStockPriceRepository;
    this.timeOperator = timeOperator;
  }

  public Optional<AccountResponse> getAccount(Integer id) {
    Optional<Account> maybeAccount = accountRepository.findById(id);
    if (maybeAccount.isEmpty()) {
      return Optional.empty();
    }

    Account acc = maybeAccount.get();
    if (acc.isDeleted()) {
      return Optional.empty();
    }

    List<Trade> trades = tradeRepository.findByAccount(acc.getId());
    BigDecimal assetValues = sumAllTrades(trades);
    return Optional.of(
        AccountResponse.builder()
            .id(acc.getId())
            .name(acc.getName())
            .accountType(acc.getAccountType())
            .cash(acc.getCash().toPlainString())
            .description(acc.getDescription())
            .currency(acc.getCurrency())
            .assetValue(assetValues.toPlainString())
            .build());
  }

  @Transactional
  public void createAccount(CreateAccountRequest request) {
    Account newAccount = new Account();
    newAccount.setAccountType(request.getAccountType());
    newAccount.setCreationTs(timeOperator.getCurrentTimestamp());
    newAccount.setName(request.getName());
    newAccount.setCurrency(request.getCurrency());
    newAccount.setDescription(request.getDescription());
    newAccount.setCash(
        request.getCash() == null ? null : toStandardRepresentation(request.getCash()));
    newAccount.setDeleted(false);

    accountRepository.save(newAccount);
  }

  @Transactional
  public void patchAccount(PatchAccountRequest request) {
    accountRepository.updateNameAndDescriptionById(
        request.getId(), request.getName(), request.getDescription());
  }

  @Transactional
  public void deleteAccount(Integer id) {
    accountRepository.softDeleteById(id);
  }

  public List<AccountResponse> getAllAccounts() {
    return accountRepository.findAll().stream()
        .filter(account -> !account.isDeleted())
        .map(
            account -> {
              AccountResponse.AccountResponseBuilder accountBuilder =
                  AccountResponse.builder()
                      .id(account.getId())
                      .name(account.getName())
                      .description(account.getDescription())
                      .accountType(account.getAccountType())
                      .cash(account.getCash().toPlainString())
                      .description(account.getDescription())
                      .currency(account.getCurrency());
              List<StockOwned> stockOwnedByAccount =
                  tradeRepository.findAllStockOwnedByAccount(account.getId());
              AtomicReference<BigDecimal> assetValue = new AtomicReference<>(BigDecimal.ZERO);
              AtomicReference<BigDecimal> costBasis = new AtomicReference<>(BigDecimal.ZERO);
              stockOwnedByAccount.forEach(
                  stockOwned -> {
                    HistoricalStockPrice latestFetchedPrice =
                        historicalStockPriceRepository.findTopByNameIsOrderByPriceTsDesc(
                            stockOwned.getStockName());
                    if (latestFetchedPrice == null) {
                      log.info("No latest price fetched for: {}", stockOwned.getStockName());
                      return;
                    }

                    BigDecimal latestPrice = latestFetchedPrice.getPrice();
                    assetValue.set(
                        assetValue
                            .get()
                            .add(
                                latestPrice.multiply(
                                    BigDecimal.valueOf(stockOwned.getNumOfUnitsHeld()))));

                    BigDecimal stockCostBasis = BigDecimal.valueOf(stockOwned.getCostBasis());
                    BigDecimal totalFeeIncurred = BigDecimal.valueOf(stockOwned.getTotalFee());
                    costBasis.set(costBasis.get().add(stockCostBasis).add(totalFeeIncurred));
                  });
              return accountBuilder
                  .assetValue(assetValue.get().toPlainString())
                  .costBasis(costBasis.get().toPlainString())
                  .build();
            })
        .toList();
  }

  @Transactional
  public void transactAccount(AccountTransactionRequest request)
      throws DataIntegrityViolationException {
    verifyAccountsExist(request);
    if (request.getTransactionType() == AccountTransactionType.DEPOSIT) {
      handleDepositAccount(request);
    } else if (request.getTransactionType() == AccountTransactionType.WITHDRAW) {
      handleWithdrawAccount(request);
    } else {
      handleTransferAccount(request);
    }
  }

  public AccountActivityPageResponse getAccountHistory(
      Integer accountId, int transactionPage, int tradePage) {
    Page<AccountTransactions> retrievedPage = null;
    Page<Trade> retrievedTradePage = null;

    if (transactionPage >= 0) {
      PageRequest accountTransactionPage =
          PageRequest.of(transactionPage, 10, Sort.by("transactionTs").descending());
      retrievedPage =
          transactionsRepository.findAllByAccountIdFrom(accountId, accountTransactionPage);
    }

    if (tradePage >= 0) {
      PageRequest accountTradePage = PageRequest.of(tradePage, 10, Sort.by("tradeTs").descending());
      retrievedTradePage = tradeRepository.findAllByAccount(accountId, accountTradePage);
    }

    log.info(
        "Fetch account history [accId={}, tranPageNum={}, tradePageNum={}], Results: [TranPage={}, TradePage={}]",
        accountId,
        transactionPage,
        tradePage,
        retrievedPage,
        retrievedTradePage);

    return AccountActivityPageResponse.builder()
        .hasNextPageForTransaction(retrievedPage != null && retrievedPage.hasNext())
        .hasNextPageForTrade(retrievedTradePage != null && retrievedTradePage.hasNext())
        .nextPageNumForTransaction(
            retrievedPage != null && retrievedPage.hasNext()
                ? retrievedPage.nextPageable().getPageNumber()
                : -1)
        .nextPageNumForTrade(
            retrievedTradePage != null && retrievedTradePage.hasNext()
                ? retrievedTradePage.nextPageable().getPageNumber()
                : -1)
        .accountTransactionsInCurrentPage(
            retrievedPage != null ? retrievedPage.getContent() : List.of())
        .accountTradesInCurrentPage(
            retrievedTradePage != null
                ? mapTradeModelToResponse(retrievedTradePage.getContent())
                : List.of())
        .build();
  }

  private void handleDepositAccount(AccountTransactionRequest request) {
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(
        request.getAccountIdFrom(), toStandardRepresentation(request.getAmount()));
  }

  private void handleWithdrawAccount(AccountTransactionRequest request) {
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(
        request.getAccountIdFrom(), toStandardRepresentation("-" + request.getAmount()));
  }

  private void handleTransferAccount(AccountTransactionRequest request) {
    Objects.requireNonNull(request.getExchangeRate());
    Objects.requireNonNull(request.getAccountIdTo());
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(
        request.getAccountIdFrom(), toStandardRepresentation("-" + request.getAmount()));
    BigDecimal amount = toStandardRepresentation(request.getAmount());
    BigDecimal exchangeRate = toStandardRepresentation(request.getExchangeRate());
    BigDecimal currencyAfterTransfer = amount.multiply(exchangeRate);
    accountRepository.updateAmountById(request.getAccountIdTo(), currencyAfterTransfer);
  }

  private void getTransactionFromRequest(AccountTransactionRequest request) {
    AccountTransactions transaction = new AccountTransactions();
    transaction.setAccountIdFrom(request.getAccountIdFrom());
    transaction.setAccountIdTo(request.getAccountIdTo());
    transaction.setTransactionTs(timeOperator.getCurrentTimestamp());
    transaction.setTransactionType(request.getTransactionType());
    transaction.setAmount(toStandardRepresentation(request.getAmount()));
    transaction.setExchangeRate(
        request.getExchangeRate() == null
            ? BigDecimal.ONE
            : toStandardRepresentation(request.getExchangeRate()));

    transactionsRepository.save(transaction);
  }

  private void verifyAccountsExist(AccountTransactionRequest request)
      throws DataIntegrityViolationException {
    Optional<Account> fromAccount = accountRepository.findById(request.getAccountIdFrom());
    if (fromAccount.isEmpty()) {
      throw new DataIntegrityViolationException(
          "Violation of "
              + BK_ACCOUNT_MUST_EXIST
              + " in account transactions: "
              + request.getAccountIdFrom());
    }

    if (request.getAccountIdTo() == null) {
      return;
    }

    Optional<Account> toAccount = accountRepository.findById(request.getAccountIdTo());
    if (toAccount.isEmpty()) {
      throw new DataIntegrityViolationException(
          "Violation of "
              + BK_ACCOUNT_MUST_EXIST
              + " in account transactions: "
              + request.getAccountIdTo());
    }
  }

  private BigDecimal sumAllTrades(List<Trade> trades) {
    return trades.stream()
        .map(
            trade ->
                (trade
                        .getPricePerUnit()
                        .multiply(new BigDecimal(trade.getNumOfUnits()))
                        .subtract(trade.getFee()))
                    .multiply(
                        trade.getTradeType() == TradeType.SELL
                            ? BigDecimal.ONE.negate()
                            : BigDecimal.ONE))
        .reduce(BigDecimal.ZERO, (BigDecimal::add));
  }

  private List<TradeResponse> mapTradeModelToResponse(List<Trade> model) {
    return model.stream()
        .map(
            trade ->
                TradeResponse.builder()
                    .tradeTs(trade.getTradeTs())
                    .tradeType(trade.getTradeType())
                    .numOfUnits(trade.getNumOfUnits())
                    .pricePerUnit(trade.getPricePerUnit().toPlainString())
                    .name(trade.getName())
                    .account(trade.getAccount())
                    .fee(trade.getFee().toPlainString())
                    .buyId(trade.getBuyId())
                    .build())
        .toList();
  }
}
