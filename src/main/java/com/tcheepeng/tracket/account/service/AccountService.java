package com.tcheepeng.tracket.account.service;

import static com.tcheepeng.tracket.common.Constants.ONE_DOLLAR_IN_MILLICENTS;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.BK_ACCOUNT_MUST_EXIST;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.account.repository.AccountTransactionsRepository;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.stock.model.Trade;
import com.tcheepeng.tracket.stock.model.TradeType;
import com.tcheepeng.tracket.stock.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final AccountTransactionsRepository transactionsRepository;
  private final TimeOperator timeOperator;
  private final TradeRepository tradeRepository;

  public AccountService(
      final AccountRepository accountRepository,
      final AccountTransactionsRepository transactionsRepository,
      final TradeRepository tradeRepository,
      final TimeOperator timeOperator) {
    this.accountRepository = accountRepository;
    this.transactionsRepository = transactionsRepository;
    this.tradeRepository = tradeRepository;
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
    int assetValues = sumAllTrades(trades);
    return Optional.of(
        AccountResponse.builder()
            .id(acc.getId())
            .name(acc.getName())
            .accountType(acc.getAccountType())
            .cashInCents(acc.getCashInCents())
            .description(acc.getDescription())
            .currency(acc.getCurrency())
            .assetValueInCents(assetValues)
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
    newAccount.setCashInCents(request.getCashInCents());
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
                      .accountType(account.getAccountType())
                      .cashInCents(account.getCashInCents())
                      .description(account.getDescription())
                      .currency(account.getCurrency());
              List<Trade> tradesByAccount = tradeRepository.findByAccount(account.getId());
              int assetValue = sumAllTrades(tradesByAccount);
              return accountBuilder.assetValueInCents(assetValue).build();
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

  private void handleDepositAccount(AccountTransactionRequest request) {
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(request.getAccountIdFrom(), request.getAmountsInCents());
  }

  private void handleWithdrawAccount(AccountTransactionRequest request) {
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(request.getAccountIdFrom(), -request.getAmountsInCents());
  }

  private void handleTransferAccount(AccountTransactionRequest request) {
    Objects.requireNonNull(request.getExchangeRateInMilli());
    Objects.requireNonNull(request.getAccountIdTo());
    getTransactionFromRequest(request);
    accountRepository.updateAmountById(request.getAccountIdFrom(), -request.getAmountsInCents());
    int currencyAfterTransfer =
        request.getAmountsInCents() * request.getExchangeRateInMilli() / ONE_DOLLAR_IN_MILLICENTS;
    accountRepository.updateAmountById(request.getAccountIdTo(), currencyAfterTransfer);
  }

  private void getTransactionFromRequest(AccountTransactionRequest request) {
    AccountTransactions transaction = new AccountTransactions();
    transaction.setAccountIdFrom(request.getAccountIdFrom());
    transaction.setAccountIdTo(request.getAccountIdTo());
    transaction.setTransactionTs(timeOperator.getCurrentTimestamp());
    transaction.setTransactionType(request.getTransactionType());
    transaction.setAmountInCents(request.getAmountsInCents());
    transaction.setExchangeRateInMilli(
        request.getExchangeRateInMilli() == null
            ? ONE_DOLLAR_IN_MILLICENTS
            : request.getExchangeRateInMilli());

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

  private int sumAllTrades(List<Trade> trades) {
    return trades.stream()
        .map(
            trade ->
                (trade.getNumOfUnits() * trade.getPricePerUnit() - trade.getFee())
                    * (trade.getTradeType() == TradeType.SELL ? -1 : 1))
        .reduce(0, (Integer::sum));
  }
}
