package com.tcheepeng.tracket.stock.model;

import com.tcheepeng.tracket.stock.api.ApiStrategy;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ticker_api", schema = "tracket")
public class TickerApi {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Basic
    @Column(name = "ticker_symbol")
    private String tickerSymbol;

    @Basic
    @Column(name = "api")
    @Enumerated(EnumType.STRING)
    private ApiStrategy api;

    @Basic
    @Column(name = "name")
    private String name;
}
