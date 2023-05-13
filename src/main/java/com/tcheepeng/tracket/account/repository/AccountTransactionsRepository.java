package com.tcheepeng.tracket.account.repository;

import com.tcheepeng.tracket.account.model.AccountTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTransactionsRepository extends JpaRepository<AccountTransactions, Integer> {}
