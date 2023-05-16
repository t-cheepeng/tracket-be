package com.tcheepeng.tracket.account.service;

import static com.tcheepeng.tracket.common.Constants.ONE_DOLLAR_IN_MILLICENTS;
import static com.tcheepeng.tracket.common.validation.BusinessValidations.BK_ACCOUNT_MUST_EXIST;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.account.repository.AccountTransactionsRepository;
import com.tcheepeng.tracket.common.service.TimeOperator;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
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

  public AccountService(
      final AccountRepository accountRepository,
      final AccountTransactionsRepository transactionsRepository,
      final TimeOperator timeOperator) {
    this.accountRepository = accountRepository;
    this.transactionsRepository = transactionsRepository;
    this.timeOperator = timeOperator;
  }

  public Optional<Account> getAccount(Integer id) {
    return accountRepository.findById(id);
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

  public List<Account> getAllAccounts() {
    return accountRepository.findAll();
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
    int currencyAfterTransfer = request.getAmountsInCents() * request.getExchangeRateInMilli() / ONE_DOLLAR_IN_MILLICENTS;
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
}
