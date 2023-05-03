package com.tcheepeng.tracket.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.common.TestHelper;
import com.tcheepeng.tracket.common.service.TimeOperator;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @InjectMocks AccountService accountService;

  @Mock AccountRepository accountRepository;

  @Mock TimeOperator timeOperator;

  @Test
  void Get_account_is_successful() {
    Optional<Account> testAccount = Optional.of(TestHelper.getTestAccount());
    when(accountRepository.findById(1)).thenReturn(testAccount);

    Optional<Account> account = accountService.getAccount(1);
    assertThat(account).isEqualTo(testAccount);
  }

  @Test
  void Get_account_no_account_is_empty() {
    when(accountRepository.findById(1)).thenReturn(Optional.empty());

    Optional<Account> account = accountService.getAccount(1);
    assertThat(account).isEqualTo(Optional.empty());
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

    assertThat(intCaptor.getValue())
            .isEqualTo(1);
  }
}
