package com.movideo.baracus.exceptions;

/**
 * Created by rranawaka on 16/12/2015.
 */
public class BaracusException extends Exception {
    public BaracusException() {
    }

    public BaracusException(String message) {
        super(message);
    }

    public BaracusException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaracusException(Throwable cause) {
        super(cause);
    }

//    public BaracusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
