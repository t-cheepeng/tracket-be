package com.tcheepeng.tracket.external.api;

public class ExternalApiException extends RuntimeException {

  public ExternalApiException() {
    super();
  }

  public ExternalApiException(String message) {
    super(message);
  }

  public ExternalApiException(Throwable cause) {
    super(cause);
  }

  public ExternalApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
