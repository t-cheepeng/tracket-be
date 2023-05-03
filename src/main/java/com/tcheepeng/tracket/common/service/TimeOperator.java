package com.tcheepeng.tracket.common.service;

import java.sql.Timestamp;import java.time.Instant;

public interface TimeOperator {
  Instant getCurrentInstant();
  Timestamp getCurrentTimestamp();
  Timestamp getTimestampFromMilliSinceEpoch(long milliSinceEpoch);
}
