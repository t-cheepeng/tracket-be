package com.tcheepeng.tracket.stock.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "trade", schema = "tracket")
public class Trade {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Basic
    @Column(name = "trade_ts")
    private Timestamp tradeTs;

    @Basic
    @Column(name = "trade_type")
    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Basic
    @Column(name = "num_of_units")
    private int numOfUnits;

    @Basic
    @Column(name = "price_per_unit")
    private BigDecimal pricePerUnit;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "account")
    private int account;

    @Basic
    @Column(name = "fee")
    private BigDecimal fee;

    @Basic
    @Column(name = "buy_id")
    private Integer buyId;

}
