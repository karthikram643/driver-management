package com.chariot.drivermanagement.util;

import com.chariot.drivermanagement.model.DriverUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private JwtConfiguration  jwtConfiguration;


    @Test
    void testGenerateASuccessfulToken() {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf( 3600));



        String token = jwtUtil.generateToken(driverUserDetails);
        Assert.assertNotNull(token);

    }

    @Test
    void extractExpiration() {
        
        
    }

    @Test
    void testExtractClaim() {
        DriverUserDetails testDriverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf(3600));

        String testToken = jwtUtil.generateToken(testDriverUserDetails);

        String validToken = jwtUtil.extractClaim(testToken, Claims::getSubject);
        Assert.assertNotNull(validToken);
    }

    @Test
    void extractUsername() {
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf(3600));
        DriverUserDetails testDriverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        String testToken = jwtUtil.generateToken(testDriverUserDetails);
        String userName = jwtUtil.extractUsername(testToken);
        Assert.assertNotNull(userName);
        Assert.assertNotNull("testuser@gmail.com",userName);
    }

    @Test
    void testValidateTokenForValidUserDetails() {
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf(3600));
        DriverUserDetails testDriverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        String testToken = jwtUtil.generateToken(testDriverUserDetails);
        boolean validToken = jwtUtil.validateToken(testToken, testDriverUserDetails);
        Assert.assertTrue(validToken);
    }

    @Test
    void testInValidateTokenForValidUserDetails() {
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf(3));
        DriverUserDetails testDriverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        String testToken = jwtUtil.generateToken(testDriverUserDetails);
        String appendInvalidString = "invalid";

        assertThrows(SignatureException.class,() -> jwtUtil.validateToken(testToken+appendInvalidString, testDriverUserDetails));
    }

    @Test
    void testValidateTokenForInValidUserDetails() {
        Mockito.when(jwtConfiguration.getSecretKey()).thenReturn("TestSecretKey");
        Mockito.when(jwtConfiguration.getExpirationWindow()).thenReturn(Long.valueOf(3));
        DriverUserDetails testDriverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        String testToken = jwtUtil.generateToken(testDriverUserDetails);
        testDriverUserDetails.getUser().setEmail("invaliduser@gmail.com");

        boolean isValid = jwtUtil.validateToken(testToken, testDriverUserDetails);

         Assert.assertFalse(isValid);
    }
}