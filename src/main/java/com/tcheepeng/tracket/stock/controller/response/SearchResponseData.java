package com.tcheepeng.tracket.stock.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import com.tcheepeng.tracket.external.api.model.ExternalSearchResponse;
import lombok.Builder;
import lombok.Data;import java.util.List;

@Data
@Builder
public class SearchResponseData implements ResponseData {
    private List<ExternalSearchResponse> searchResponses;
}
