package com.tcheepeng.tracket.stock.repository;

import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import com.tcheepeng.tracket.stock.model.TickerApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TickerApiRepository extends JpaRepository<TickerApi,Integer> {

    @Query(value = "select api from TickerApi api inner join Stock stock on api.name=stock.name")
    List<TickerApi> findApiStock();

    List<TickerApi> findAllByApiEqualsAndStockIsDeletedFalse(ApiFetcher api);
}
