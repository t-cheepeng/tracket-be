package com.tcheepeng.tracket.account.service;

import static com.tcheepeng.tracket.common.Constants.ONE_DOLLAR_IN_MILLICENTS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcheepeng.tracket.account.controller.request.AccountTransactionRequest;
import com.tcheepeng.tracket.account.controller.response.AccountResponse;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.model.AccountTransactionType;
import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.account.model.AccountType;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.account.repository.AccountTransactionsRepository;
import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.common.service.TimeOperator;
import com.tcheepeng.tracket.stock.repository.TradeRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @InjectMocks AccountService accountService;

  @Mock AccountRepository accountRepository;

  @Mock AccountTransactionsRepository transactionsRepository;

  @Mock TradeRepository tradeRepository;

  @Mock TimeOperator timeOperator;

  @Test
  void Get_account_is_successful() {
    Optional<Account> testAccount = Optional.of(TestHelper.getTestAccount());
    when(accountRepository.findById(1)).thenReturn(testAccount);
    when(tradeRepository.findByAccount(1)).thenReturn(TestHelper.getAllTradesForTestAccount());

    Optional<AccountResponse> account = accountService.getAccount(1);
    AccountResponse expected =
        AccountResponse.builder()
            .id(1)
            .name("Test Account")
            .currency("SGD")
            .accountType(AccountType.INVESTMENT)
            .description("Account description")
            .cashInCents(1000)
            .assetValueInCents(1000)
            .build();
    assertThat(account).isNotEmpty().isEqualTo(expected);
  }

  @Test
  void Get_account_no_account_is_empty() {
    when(accountRepository.findById(1)).thenReturn(Optional.empty());

    Optional<AccountResponse> account = accountService.getAccount(1);
    assertThat(account).isEmpty();
  }

  @Test
  void Get_deleted_account_is_empty() {
    Account account = TestHelper.getTestAccount();
    account.setDeleted(true);
    when(accountRepository.findById(1)).thenReturn(Optional.of(account));

    Optional<AccountResponse> result = accountService.getAccount(1);

    assertThat(result).isEmpty();
  }

  @Test
  void Create_account_is_successful() {
    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    Timestamp expectedTimestamp = Timestamp.from(Instant.ofEpochMilli(1000));

    when(accountRepository.save(captor.capture())).thenReturn(TestHelper.getTestAccount());
    when(timeOperator.getCurrentTimestamp()).thenReturn(expectedTimestamp);

    accountService.createAccount(TestHelper.getCreateAccountRequest());

    assertThat(captor.getValue())
        .returns(expectedTimestamp, from(Account::getCreationTs))
        .returns(false, from(Account::isDeleted));
  }

  @Test
  void Patch_account_is_successful() {
    ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    doNothing()
        .when(accountRepository)
        .updateNameAndDescriptionById(intCaptor.capture(), captor.capture(), captor.capture());

    accountService.patchAccount(TestHelper.getPatchAccountRequest());

    assertThat(captor.getAllValues()).containsOnlyOnce("Test change name", "Test change desc");
    assertThat(intCaptor.getValue()).isEqualTo(1);
  }

  @Test
  void Delete_account_is_successful() {
    ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);

    doNothing().when(accountRepository).softDeleteById(intCaptor.capture());

    accountService.deleteAccount(1);

    assertThat(intCaptor.getValue()).isEqualTo(1);
  }

  @Test
  void Deposit_transaction_is_successful() {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    when(timeOperator.getCurrentTimestamp()).thenReturn(Timestamp.from(Instant.ofEpochMilli(1000)));
    when(accountRepository.findById((anyInt())))
        .thenReturn(Optional.of(TestHelper.getTestAccount()));

    accountService.transactAccount(request);

    ArgumentCaptor<AccountTransactions> transactionCaptor =
        ArgumentCaptor.forClass(AccountTransactions.class);
    verify(transactionsRepository).save(transactionCaptor.capture());
    AccountTransactions expectedTransaction = new AccountTransactions();
    expectedTransaction.setTransactionTs(Timestamp.from(Instant.ofEpochMilli(1000)));
    expectedTransaction.setAccountIdTo(null);
    expectedTransaction.setAccountIdFrom(0);
    expectedTransaction.setAmountInCents(1000);
    expectedTransaction.setTransactionType(AccountTransactionType.DEPOSIT);
    expectedTransaction.setExchangeRateInMilli(ONE_DOLLAR_IN_MILLICENTS);

    assertThat(transactionCaptor.getValue()).isEqualTo(expectedTransaction);
    verify(accountRepository).updateAmountById(0, 1000);
  }

  @Test
  void Withdraw_transaction_is_successful() {
    AccountTransactionRequest request = TestHelper.getWithdrawRequest();
    when(timeOperator.getCurrentTimestamp()).thenReturn(Timestamp.from(Instant.ofEpochMilli(1000)));
    when(accountRepository.findById((anyInt())))
        .thenReturn(Optional.of(TestHelper.getTestAccount()));

    accountService.transactAccount(request);

    ArgumentCaptor<AccountTransactions> transactionCaptor =
        ArgumentCaptor.forClass(AccountTransactions.class);
    verify(transactionsRepository).save(transactionCaptor.capture());
    AccountTransactions expectedTransaction = new AccountTransactions();
    expectedTransaction.setTransactionTs(Timestamp.from(Instant.ofEpochMilli(1000)));
    expectedTransaction.setAccountIdTo(null);
    expectedTransaction.setAccountIdFrom(0);
    expectedTransaction.setAmountInCents(1000);
    expectedTransaction.setTransactionType(AccountTransactionType.WITHDRAW);
    expectedTransaction.setExchangeRateInMilli(ONE_DOLLAR_IN_MILLICENTS);

    assertThat(transactionCaptor.getValue()).isEqualTo(expectedTransaction);
    verify(accountRepository).updateAmountById(0, -1000);
  }

  @Test
  void Transaction_to_account_that_does_not_exist_fails() {
    AccountTransactionRequest request = TestHelper.getDepositRequest();
    when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> accountService.transactAccount(request))
        .withMessage("Violation of BK_ACCOUNT_MUST_EXIST in account transactions: 0");
  }

  @Test
  void Transfer_to_account_is_successful() {
    AccountTransactionRequest request = TestHelper.getTransferRequest();
    assertThat(request.getAccountIdTo()).isNotNull();
    Account accountFrom = TestHelper.getTestAccount();
    Account accountTo = TestHelper.getTestAccount();
    accountTo.setId(1);

    when(timeOperator.getCurrentTimestamp()).thenReturn(Timestamp.from(Instant.ofEpochMilli(1000)));
    when(accountRepository.findById(request.getAccountIdFrom()))
        .thenReturn(Optional.of(accountFrom));
    when(accountRepository.findById(request.getAccountIdTo())).thenReturn(Optional.of(accountTo));

    accountService.transactAccount(request);

    ArgumentCaptor<AccountTransactions> transactionCaptor =
        ArgumentCaptor.forClass(AccountTransactions.class);
    verify(transactionsRepository).save(transactionCaptor.capture());
    AccountTransactions expectedTransaction = new AccountTransactions();
    expectedTransaction.setTransactionTs(Timestamp.from(Instant.ofEpochMilli(1000)));
    expectedTransaction.setAccountIdTo(1);
    expectedTransaction.setAccountIdFrom(0);
    expectedTransaction.setAmountInCents(254);
    expectedTransaction.setExchangeRateInMilli(74560);
    expectedTransaction.setTransactionType(AccountTransactionType.TRANSFER);

    assertThat(transactionCaptor.getValue()).isEqualTo(expectedTransaction);
    verify(accountRepository).updateAmountById(0, -254);
    verify(accountRepository).updateAmountById(1, 189);
  }
}
