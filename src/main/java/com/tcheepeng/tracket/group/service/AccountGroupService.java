package com.tcheepeng.tracket.group.service;

import com.tcheepeng.tracket.group.controller.request.CreateAccountAccountGroupRequest;
import com.tcheepeng.tracket.group.controller.request.CreateAccountGroupRequest;
import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import com.tcheepeng.tracket.group.model.AccountGroup;
import com.tcheepeng.tracket.group.repository.AccountAccountGroupRepository;
import com.tcheepeng.tracket.group.repository.AccountGroupRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountGroupService {

  private final AccountGroupRepository accountGroupRepository;
  private final AccountAccountGroupRepository accountAccountGroupRepository;

  public AccountGroupService(
      final AccountGroupRepository accountGroupRepository,
      final AccountAccountGroupRepository accountAccountGroupRepository) {
    this.accountGroupRepository = accountGroupRepository;
    this.accountAccountGroupRepository = accountAccountGroupRepository;
  }

  public void createAccountGroup(CreateAccountGroupRequest request) {
    AccountGroup group = new AccountGroup();
    group.setName(request.getName());
    group.setCurrency(request.getCurrency());
    accountGroupRepository.save(group);
  }

  public void groupAccount(CreateAccountAccountGroupRequest request) {
    AccountAccountGroup.EmbeddedAccountAccountGroup group =
        new AccountAccountGroup.EmbeddedAccountAccountGroup();
    group.setAccountId(request.getAccountId());
    group.setAccountGroupId(request.getAccountGroupId());
    AccountAccountGroup groupEntity = new AccountAccountGroup();
    groupEntity.setAccountAccountGroup(group);
    accountAccountGroupRepository.save(groupEntity);
  }
}
