package com.tcheepeng.tracket.external.repository;

import com.tcheepeng.tracket.external.model.SgxFetchPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SgxFetchPriceHistoryRepository extends JpaRepository<SgxFetchPriceHistory, Integer> {

    SgxFetchPriceHistory findTopByOrderByDateOfPathCodeDesc();
}
