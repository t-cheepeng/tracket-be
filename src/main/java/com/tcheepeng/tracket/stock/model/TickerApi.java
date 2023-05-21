package com.tcheepeng.tracket.stock.model;

import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
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
    private ApiFetcher api;

    @Basic
    @Column(name = "name")
    private String name;

    @OneToOne()
    @JoinColumn(name="name", insertable = false, updatable = false)
    private Stock stock;
}
