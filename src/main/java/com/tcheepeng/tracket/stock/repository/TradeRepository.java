package com.tcheepeng.tracket.stock.repository;

import com.tcheepeng.tracket.stock.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Integer> {}
