package com.tcheepeng.tracket.common.exceptions;

public class TracketServiceException extends Exception {
    public TracketServiceException(String message) {
        super(message);
    }

    public TracketServiceException(Throwable cause) {
        super(cause);
    }
}
