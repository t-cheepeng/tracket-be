package com.tcheepeng.tracket.stock.repository;

import com.tcheepeng.tracket.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, String> {
  @Modifying
  @Query("UPDATE Stock s SET s.isDeleted=true WHERE s.name=:name")
  void softDeleteByName(@Param(value = "name") String name);
}
