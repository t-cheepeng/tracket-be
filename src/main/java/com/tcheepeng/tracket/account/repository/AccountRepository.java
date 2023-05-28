package com.tcheepeng.tracket.account.repository;

import com.tcheepeng.tracket.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AccountRepository extends JpaRepository<Account, Integer> {

  @Modifying
  @Query("UPDATE Account a SET a.name=:name, a.description=:description WHERE a.id=:id")
  void updateNameAndDescriptionById(
      @Param(value = "id") int id,
      @Param(value = "name") String name,
      @Param(value = "description") String description);

  @Modifying
  @Query("UPDATE Account a SET a.isDeleted=true WHERE a.id=:id")
  void softDeleteById(@Param(value = "id") int id);

  @Modifying
  @Query("Update Account a SET a.cash=a.cash + :amount WHERE a.id=:id")
  void updateAmountById(@Param(value = "id") int id, @Param(value = "amount") BigDecimal amount);
}
