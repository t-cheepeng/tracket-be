package com.tcheepeng.tracket.stock.api;

public class StockApiException extends RuntimeException {

  public StockApiException() {
    super();
  }

  public StockApiException(String message) {
    super(message);
  }

  public StockApiException(Throwable cause) {
    super(cause);
  }

  public StockApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
