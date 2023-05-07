package com.chariot.drivermanagement.controller;

import com.chariot.drivermanagement.configuration.SecurityConfigurer;
import com.chariot.drivermanagement.dto.DriverReadinessDTO;
import com.chariot.drivermanagement.dto.DriverRegistrationDTO;
import com.chariot.drivermanagement.exception.KafkaConnectionException;
import com.chariot.drivermanagement.exception.UserAlreadyExistsException;
import com.chariot.drivermanagement.kafka.KafkaProducer;
import com.chariot.drivermanagement.model.*;
import com.chariot.drivermanagement.service.DriverUserDetailsService;
import com.chariot.drivermanagement.util.JwtUtil;
import com.chariot.drivermanagement.util.TestUtil;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@RunWith(SpringRunner.class)
class DriverResourceManagementControllerTest {

    @InjectMocks
    private DriverResourceManagementController driverResourceManagementController;

    @Mock
    private DriverUserDetailsService driverUserDetailsService;

    @Mock
    private SecurityConfigurer securityConfigurer;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KafkaProducer kafkaProducer;

   private DriverSubscriptionResponse driverSubscriptionResponse;

   private DriverRegistrationDTO driverRegistrationDTO;

    final String authorizationToken = "testJwtasdfadsfadsfasdfkasdfadfasdfdsafdsafadsf";


    @BeforeClass
    void init(){
        driverSubscriptionResponse = TestUtil.fetchDriverSubscriptionResponse();
        driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();

    }

    @Test
    void registerDriverDetails_Successfully() throws UserAlreadyExistsException {
        driverSubscriptionResponse = TestUtil.fetchDriverSubscriptionResponse();
        driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();

        Mockito.when(driverUserDetailsService.saveUniqueDriverDetailsToDB(Mockito.any()))
                .thenReturn(driverSubscriptionResponse);
        Mockito.doNothing().when(kafkaProducer).sendMessage(Mockito.anyString(),
                Mockito.any(DriverSubscriptionResponse.class));


        ResponseEntity<?> driverSubscribedResponseEntity = driverResourceManagementController
                .registerDriverDetails(driverRegistrationDTO);
        DriverSubscriptionResponse driverSubscriptionResponse = (DriverSubscriptionResponse) driverSubscribedResponseEntity.getBody();

        Assert.assertNotNull(driverSubscribedResponseEntity);
        Assert.assertEquals(driverRegistrationDTO.getEmail(),driverSubscriptionResponse.getEmail() );
    }

    @Test
    void registerDriverDetails_ThrowsUserAlreadyExistsException_when_DuplicateUserDetails() throws UserAlreadyExistsException {
        driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();

        BDDMockito.when(driverUserDetailsService.saveUniqueDriverDetailsToDB(Mockito.any()))
                .thenThrow(new UserAlreadyExistsException(String.format("Driver with email Id %s already exists in the system." +
                        "Please sign up with a different email address",driverRegistrationDTO.getEmail())));
        Mockito.doNothing().when(kafkaProducer).sendMessage(Mockito.anyString(),
                Mockito.any(DriverSubscriptionResponse.class));

        assertThrows(UserAlreadyExistsException.class, () -> driverResourceManagementController
                        .registerDriverDetails(driverRegistrationDTO));

    }

    @Test
    void registerDriverDetails_ThrowsKafkaConnectionException_when_DuplicateUserDetails() throws UserAlreadyExistsException {
        driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();
        driverSubscriptionResponse = TestUtil.fetchDriverSubscriptionResponse();

        String serverUnavailableMessage = String.format("There seems to be a problem from our side, " +
                "please try saving details for %s again", driverRegistrationDTO.getEmail());
        Mockito.when(driverUserDetailsService.saveUniqueDriverDetailsToDB(Mockito.any()))
                .thenReturn(driverSubscriptionResponse);
        Mockito.when(driverUserDetailsService.deleteDriverFromDB(Mockito.any()))
                .thenReturn(driverSubscriptionResponse);

        Mockito.doThrow(new KafkaConnectionException(serverUnavailableMessage))
                .when(kafkaProducer).sendMessage(Mockito.anyString(),Mockito.any(DriverSubscriptionResponse.class));

        assertThrows(KafkaConnectionException.class, () -> driverResourceManagementController
                .registerDriverDetails(driverRegistrationDTO));
    }

    @Test
    void create_Successful_AuthenticationToken() {

        final String jwt = "testJwtasdfadsfadsfasdfkasdfadfasdfdsafdsafadsf";

        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(authenticationManager.authenticate
                        (Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser@gmail.com",
                        "validPassword"));
        Mockito.when(driverUserDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(driverUserDetails);
        Mockito.when(jwtUtil.generateToken(Mockito.any(UserDetails.class)))
                .thenReturn(jwt);

        AuthenticationRequest authenticationRequest = TestUtil.fetchAuthenticationRequestForValidTestUser();
        ResponseEntity<?> authenticationToken = driverResourceManagementController.createAuthenticationToken(authenticationRequest);
        AuthenticationResponse response = ( AuthenticationResponse) authenticationToken.getBody();
        Assert.assertNotNull(authenticationToken);
        Assert.assertEquals(jwt, response.getJwt());

    }

    @Test
    void charioteerReadiness_Fail_When_TokenExpired() throws Exception {

        DriverReadinessDTO driverReadinessDTO = TestUtil.fetchDriverReadinessDTO(true);
        final String expiredToken = "testJwtasdfadsfadsfasdfkasdfadfasdfdsafdsafadsf";
        final String testUserName = "testuser@gmail.com";

        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(authenticationManager.authenticate
                        (Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser@gmail.com",
                        "validPassword"));
        Mockito.when(driverUserDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(driverUserDetails);
        Mockito.when(jwtUtil.generateToken(Mockito.any(UserDetails.class)))
                .thenReturn(expiredToken);

        ResponseEntity<?> httpResponse = driverResourceManagementController.charioteerReadiness(expiredToken,
                testUserName, driverReadinessDTO);
        String responseMessage = ( String) httpResponse.getBody();
        Assert.assertNotNull(responseMessage);
        String successfulMessage = "The token was not valid. Try authenticating once again";
        System.out.println(responseMessage);
        Assert.assertEquals(successfulMessage, responseMessage);
    }


    @Test
    void charioteerReadiness_throw_And_Handle_UsernameNotFundException_When_invalidUser() throws Exception {

        DriverReadinessDTO driverReadinessDTO = TestUtil.fetchDriverReadinessDTO(true);
        final String authorization = "testJwtasdfadsfadsfasdfkasdfadfasdfdsafdsafadsf";
        final String testUserName = "testuser@gmail.com";

        TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(authenticationManager.authenticate
                        (Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser@gmail.com",
                        "validPassword"));
        Mockito.when(driverUserDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenThrow(UsernameNotFoundException.class);

        assertThrows(UsernameNotFoundException.class,() ->  driverResourceManagementController.charioteerReadiness(authorization,
                testUserName, driverReadinessDTO));
    }

    @Test
    void charioteerReadiness_SetTo_True_ForValidDriver() throws Exception {

        DriverReadinessDTO driverReadinessDTO = TestUtil.fetchDriverReadinessDTO(true);
        final String testUserName = "testuser@gmail.com";

        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(authenticationManager.authenticate
                        (Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser@gmail.com",
                        "validPassword"));
        Mockito.when(driverUserDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(driverUserDetails);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.any(UserDetails.class)))
                .thenReturn(true);
        DriverVerifiedAndAvailableDetailsResponse driverAvailableResponse = TestUtil.fetchDriverVerifiedAndAvailableDetailsResponseForTest();
        Mockito.when(driverUserDetailsService.updateDriverReadinessToTakeRide(Mockito.any(UserDetails.class),Mockito.anyBoolean()))
                .thenReturn(driverAvailableResponse);

        ResponseEntity<?> response = driverResourceManagementController.charioteerReadiness(authorizationToken,
                testUserName, driverReadinessDTO);
        String apiResponseMessage = ( String) response.getBody();
        Assert.assertNotNull(apiResponseMessage);
        String responseMessage = String.format("%s is now ready to take a ride request", testUserName);
        System.out.println(responseMessage);
        Assert.assertEquals(responseMessage, apiResponseMessage);
    }

    @Test
    void charioteerReadiness_SetTo_False_ForValidDriver() throws Exception {

        DriverReadinessDTO driverReadinessDTO = TestUtil.fetchDriverReadinessDTO(false);
        final String testUserName = "testuser@gmail.com";

        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        Mockito.when(authenticationManager.authenticate
                        (Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser@gmail.com",
                        "validPassword"));
        Mockito.when(driverUserDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(driverUserDetails);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.any(UserDetails.class)))
                .thenReturn(true);
        DriverVerifiedAndAvailableDetailsResponse driverAvailableResponse = TestUtil.fetchDriverVerifiedAndAvailableDetailsResponseForTest();
        Mockito.when(driverUserDetailsService.updateDriverReadinessToTakeRide(Mockito.any(UserDetails.class),Mockito.anyBoolean()))
                .thenReturn(driverAvailableResponse);

        ResponseEntity<?> response = driverResourceManagementController.charioteerReadiness(authorizationToken,
                testUserName, driverReadinessDTO);
        String apiResponseMessage = ( String) response.getBody();
        Assert.assertNotNull(apiResponseMessage);
        String responseMessage = String.format("%s is marked unavailable to take a ride request", testUserName);
        System.out.println(responseMessage);
        Assert.assertEquals(responseMessage, apiResponseMessage);
    }

    public ResponseEntity<?> fallBackForUserSubscription(KafkaConnectionException e){
        String circuitBreakerMessage = String.format("Our servers seems to be down. Please give us sometime " +
                "while we are trying to fix it. Comeback later and try registering. ");
        throw new KafkaConnectionException(circuitBreakerMessage);
    }

    @Test
    void testFallBackForUserSubscription() {
        String kafkaMessage = "Our servers seems to be down. Please give us sometime " +
                "while we are trying to fix it. Comeback later and try registering.";

        assertThrows(KafkaConnectionException.class,() -> driverResourceManagementController
                .fallBackForUserSubscription(new KafkaConnectionException(kafkaMessage)));
    }

    @Test
    void testFallBackForMarkingReadyRateLimitation() {
        String rateLimiterName = "testRateLimiter";
        String rateLimiterMessage = String.format("Perhaps you tried to mark ready too many times " +
                "that could yield undesired result. Please wait for sometime and mark your availability carefully");
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.ofDefaults();
        ResponseEntity<?> responseFromRateLimiter = driverResourceManagementController
                .fallBackForMarkingReadyRateLimitation(RequestNotPermitted.createRequestNotPermitted(
                        RateLimiter.of(rateLimiterName, rateLimiterConfig)));
        String responseMessage = (String) responseFromRateLimiter.getBody();
        Assert.assertEquals(rateLimiterMessage, responseMessage);
    }
}