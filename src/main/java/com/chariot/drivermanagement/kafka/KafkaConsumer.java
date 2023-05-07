package com.chariot.drivermanagement.kafka;

import com.chariot.drivermanagement.service.DriverUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    private DriverUserDetailsService userDetailsService;


    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "driver-registration-verification", groupId = "verification-consumers")
    public void consume(String message) {
        LOGGER.info(String.format("Registration Verification for user %s", message));

        try {
            boolean isDriverVerified = true;
            userDetailsService.updateDriverVerifiedInTheSystem(message,isDriverVerified);
        } catch (UsernameNotFoundException usernameNotFoundException) {
            LOGGER.info(usernameNotFoundException.getMessage());
        }
    }
}
