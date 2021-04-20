package com.dosse.speedtest.widget.json;

public class JSONException extends RuntimeException {

    public JSONException(String s) {
        super(s);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
