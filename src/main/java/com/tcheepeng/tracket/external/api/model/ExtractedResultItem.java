package com.tcheepeng.tracket.external.api.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@EqualsAndHashCode
public class ExtractedResultItem<T> implements Comparable<ExtractedResult> {

  @EqualsExclude private ExtractedResult result;
  private T item;

  @Override
  public int compareTo(@NotNull ExtractedResult o) {
    return this.result.compareTo(o);
  }
}
