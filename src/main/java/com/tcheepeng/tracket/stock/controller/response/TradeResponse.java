package com.tcheepeng.tracket.stock.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import com.tcheepeng.tracket.stock.model.TradeType;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class TradeResponse implements ResponseData {
    private Timestamp tradeTs;
    private TradeType tradeType;
    private int numOfUnits;
    private int pricePerUnit;
    private String name;
    private int account;
    private int fee;
    private Integer buyId;
}
