package com.tcheepeng.tracket.stock.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StocksResponse implements ResponseData {
    private List<StockResponse> stocks;
}
