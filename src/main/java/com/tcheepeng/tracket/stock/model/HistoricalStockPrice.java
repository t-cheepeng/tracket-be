package com.tcheepeng.tracket.stock.model;

import com.tcheepeng.tracket.external.api.fetcher.ApiFetcher;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "historical_stock_price", schema = "tracket")
public class HistoricalStockPrice {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private int id;

  @Basic
  @Column(name = "price_ts")
  private Timestamp priceTs;

  @Basic
  @Column(name = "price")
  private BigDecimal price;

  @Basic
  @Column(name = "name")
  private String name;

  @Basic
  @Column(name = "api")
  @Enumerated(EnumType.STRING)
  private ApiFetcher api;
}
