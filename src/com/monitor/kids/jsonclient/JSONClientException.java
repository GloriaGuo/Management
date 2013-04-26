package com.monitor.kids.jsonclient;

public class JSONClientException extends Exception {
    
    private static final long serialVersionUID = 4657697652848090922L;

    public JSONClientException(Object error)
    {
        super(error.toString());
    }
    
    public JSONClientException(String message, Throwable innerException)
    {
        super(message, innerException);
    }

}
