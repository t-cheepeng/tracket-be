package com.tcheepeng.tracket.stock.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stock", schema = "tracket")
public class Stock {
    @Id
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "currency")
    private String currency;

    @Basic
    @Column(name = "asset_class")
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;

    @Basic
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Basic
    @Column(name = "display_ticker_symbol")
    private String displayTickerSymbol;

}
