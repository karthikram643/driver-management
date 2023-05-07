package com.chariot.drivermanagement.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


@RunWith(SpringRunner.class)
@SpringBootTest
class JwtConfigurationTest {

    @InjectMocks
    private JwtConfiguration jwtConfiguration;

    @Test
    void getExpirationWindow() {
        Field expirationWindow = ReflectionUtils.findField(JwtConfiguration.class, "EXPIRATION_WINDOW");
        ReflectionUtils.makeAccessible(expirationWindow);
        ReflectionUtils.setField(expirationWindow,jwtConfiguration,Long.valueOf(3600));
        long result = jwtConfiguration.getExpirationWindow();
        Assert.assertNotNull(result);
        Assert.assertEquals(3600,result);
    }

    @Test
    void getSecretKey() {
        String testSecretKey = "SecretKey";
        Field secretKey = ReflectionUtils.findField(JwtConfiguration.class, "SECRET_KEY");
        ReflectionUtils.makeAccessible(secretKey);
        ReflectionUtils.setField(secretKey,jwtConfiguration,testSecretKey);
        String result = jwtConfiguration.getSecretKey();
        Assert.assertNotNull(result);
        Assert.assertEquals(testSecretKey,result);
    }
}