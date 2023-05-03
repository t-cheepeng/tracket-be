package com.tcheepeng.tracket.common.service;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class TimeOperatorService implements TimeOperator {

  @Override
  public Instant getCurrentInstant() {
        return Instant.now();
    }

  @Override
  public Timestamp getCurrentTimestamp() {
    return Timestamp.from(getCurrentInstant());
  }

  @Override
  public Timestamp getTimestampFromMilliSinceEpoch(long milliSinceEpoch) {
    return Timestamp.from(Instant.ofEpochMilli(milliSinceEpoch));
  }
}
