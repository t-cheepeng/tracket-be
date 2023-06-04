package com.tcheepeng.tracket.stock.repository;

import com.tcheepeng.tracket.stock.model.HistoricalStockPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalStockPriceRepository extends JpaRepository<HistoricalStockPrice, Integer> {

    HistoricalStockPrice findTopByNameIsOrderByPriceTsDesc(String stockName);
}
