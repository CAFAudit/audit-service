package com.hpe.caf.services.audit.api.generated;

/**
 * Created by CS on 14/04/2016.
 */
public class ApiResponseMessage {

    private int partitionsAdded;
    private String message;

    public ApiResponseMessage(){}

    public ApiResponseMessage(int partitionsAdded) {
        this.partitionsAdded = partitionsAdded;
    }

    public ApiResponseMessage(String message) {
        this.message = message;
    }

    public ApiResponseMessage(int partitionsAdded, String message) {
        this.partitionsAdded = partitionsAdded;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPartitionsAdded() {
        return partitionsAdded;
    }

    public void setPartitionsAdded(int partitionsAdded) {
        this.partitionsAdded = partitionsAdded;
    }
}
