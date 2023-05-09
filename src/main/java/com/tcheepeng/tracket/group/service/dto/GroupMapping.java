package com.tcheepeng.tracket.group.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupMapping {

    private int id;
    private String name;
    private String currency;
    private List<Integer> accountIdUnderGroup;
}
