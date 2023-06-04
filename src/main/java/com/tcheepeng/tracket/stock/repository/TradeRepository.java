package com.tcheepeng.tracket.stock.repository;

import com.tcheepeng.tracket.account.model.AccountTransactions;
import com.tcheepeng.tracket.account.model.StockOwned;
import com.tcheepeng.tracket.stock.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Integer> {

    List<Trade> findByAccount(int account);

    @Query(value = """
                SELECT
                    trade.name AS stockName,
                    stock.currency AS currency,
                    stock.assetClass as assetClass,
                    SUM(CASE WHEN trade.tradeType = 'SELL' THEN -trade.numOfUnits WHEN trade.tradeType = 'DIVIDEND' THEN 0 ELSE trade.numOfUnits END) AS numOfUnitsHeld,
                    SUM(trade.fee) AS totalFee,
                    SUM(CASE WHEN trade.tradeType = 'SELL' THEN -1 * trade.numOfUnits * trade.pricePerUnit ELSE trade.numOfUnits * trade.pricePerUnit END) AS costBasis
                FROM Trade trade
                    INNER JOIN Account account ON trade.account = account.id
                    INNER JOIN Stock stock ON trade.name = stock.name
                WHERE
                    account.id = :account AND
                    stock.isDeleted = false AND
                    account.isDeleted = false
                GROUP BY
                    trade.name,
                    stock.currency,
                    stock.assetClass
            """)
    List<StockOwned> findAllStockOwnedByAccount(int account);

    Page<Trade> findAllByAccount(int accountId, Pageable pageable);
}
