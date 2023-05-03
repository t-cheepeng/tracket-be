package com.tcheepeng.tracket.account.service;

import com.tcheepeng.tracket.account.controller.request.CreateAccountRequest;
import com.tcheepeng.tracket.account.controller.request.PatchAccountRequest;
import com.tcheepeng.tracket.account.model.Account;
import com.tcheepeng.tracket.account.repository.AccountRepository;
import com.tcheepeng.tracket.common.service.TimeOperator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final TimeOperator timeOperator;

  public AccountService(
      final AccountRepository accountRepository, final TimeOperator timeOperator) {
    this.accountRepository = accountRepository;
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
}
