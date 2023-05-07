package com.chariot.drivermanagement.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfiguration {

    @Value("${jwt.expirationWindow}")
    private Long EXPIRATION_WINDOW;
    @Value("${jwt.secretKey}")
    private String SECRET_KEY;

    public long getExpirationWindow() {
        return EXPIRATION_WINDOW;
    }

    public String getSecretKey() {
        return SECRET_KEY;
    }
}
