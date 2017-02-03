package com.hpe.testapplication.auditing;

import java.util.List;

/**
 * Class to represent messages in test data YAML file.
 */
public class AuditEventMessages {

    public AuditEventMessages() {
    }

    public AuditEventMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    private int numberOfMessages;
    private List<AuditEventMessage> messages;

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public List<AuditEventMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AuditEventMessage> messages) {
        this.messages = messages;
    }

}
