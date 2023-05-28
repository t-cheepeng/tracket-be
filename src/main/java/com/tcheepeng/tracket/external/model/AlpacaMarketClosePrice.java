package com.tcheepeng.tracket.external.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
public class AlpacaMarketClosePrice {
    private String tickerSymbol;
    private BigDecimal price;
    private Timestamp indicativePriceTs;
}
