package com.tcheepeng.tracket.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Utils {

  private Utils() {}

  public static BigDecimal toStandardRepresentation(String amount) {
    MathContext context = new MathContext(13, RoundingMode.HALF_EVEN);
    return new BigDecimal(amount, context).setScale(6, RoundingMode.HALF_EVEN);
  }
}
