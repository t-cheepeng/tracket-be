package com.tcheepeng.tracket.group.controller.response;

import com.tcheepeng.tracket.common.response.ResponseData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupMappingsResponse implements ResponseData {
    private List<GroupMappingResponse> groupMappings;
}
