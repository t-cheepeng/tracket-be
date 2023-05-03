package com.tcheepeng.tracket.group.repository;

import com.tcheepeng.tracket.group.model.AccountAccountGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountAccountGroupRepository
    extends JpaRepository<AccountAccountGroup, AccountAccountGroup.EmbeddedAccountAccountGroup> {}
