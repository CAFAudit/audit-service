package com.hpe.caf.services.audit.api.generated;

/**
 * Created by CS on 14/04/2016.
 */
public class ApiPartitionResponseMessage {

    private int partitionsAdded;
    private String message;

    public ApiPartitionResponseMessage(){}

    public ApiPartitionResponseMessage(int partitionsAdded) {
        this.partitionsAdded = partitionsAdded;
    }

    public ApiPartitionResponseMessage(String message) {
        this.message = message;
    }

    public ApiPartitionResponseMessage(int partitionsAdded, String message) {
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
