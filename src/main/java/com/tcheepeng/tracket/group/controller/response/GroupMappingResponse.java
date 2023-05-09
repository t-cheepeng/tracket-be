package com.tcheepeng.tracket.group.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupMappingResponse implements ResponseData {
    private String name;
    private String currency;
    private List<Integer> accountIdUnderGroup;
}
