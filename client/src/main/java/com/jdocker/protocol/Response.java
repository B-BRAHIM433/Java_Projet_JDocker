package com.jdocker.protocol;

public class Response {

    private String status;
    private String message;
    private Object payload;

    public Response() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Status: " + status + " | Message: " + message + " | Payload: " + payload;
    }
}
