package com.chariot.drivermanagement.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends UsernameNotFoundException {

    private String detailedMessage;

    public UserNotFoundException(String msg, String detailedMessage) {
        super(msg);
        this.detailedMessage = detailedMessage;
    }

    public UserNotFoundException(String msg, Throwable cause, String detailedMessage) {
        super(msg, cause);
        this.detailedMessage = detailedMessage;
    }

}
