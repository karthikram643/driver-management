package com.chariot.drivermanagement.exception;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.TimeoutException;

public class KafkaConnectionException extends KafkaException {


    private String detailedMessage;

    public KafkaConnectionException(String detailedMessage) {
        super();
        this.detailedMessage = detailedMessage;
    }

    public KafkaConnectionException(String message, Throwable cause, String detailedMessage) {
        super(message, cause);
        this.detailedMessage = detailedMessage;
    }

    public KafkaConnectionException(String message, String detailedMessage) {
        super(message);
        this.detailedMessage = detailedMessage;
    }

    public KafkaConnectionException(Throwable cause, String detailedMessage) {
        super(cause);
        this.detailedMessage = detailedMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }
}
