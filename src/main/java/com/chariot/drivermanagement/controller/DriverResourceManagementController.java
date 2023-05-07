package com.chariot.drivermanagement.controller;

import com.chariot.drivermanagement.dto.DriverReadinessDTO;
import com.chariot.drivermanagement.dto.DriverRegistrationDTO;
import com.chariot.drivermanagement.exception.DriverNotVerifiedException;
import com.chariot.drivermanagement.exception.KafkaConnectionException;
import com.chariot.drivermanagement.exception.UserAlreadyExistsException;
import com.chariot.drivermanagement.kafka.KafkaProducer;
import com.chariot.drivermanagement.model.AuthenticationRequest;
import com.chariot.drivermanagement.model.AuthenticationResponse;
import com.chariot.drivermanagement.model.DriverSubscriptionResponse;
import com.chariot.drivermanagement.model.DriverVerifiedAndAvailableDetailsResponse;
import com.chariot.drivermanagement.service.DriverUserDetailsService;
import com.chariot.drivermanagement.util.JwtUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/chariot")
@Validated
public class DriverResourceManagementController {

    private static final Logger logger = LoggerFactory.getLogger(DriverResourceManagementController.class);


    @Autowired
    private DriverUserDetailsService driverUserDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KafkaProducer kafkaProducer;

    private final static String KAFKA_TOPIC = "driver-subscription";

    private final static String KAFKA_CIRCUIT_BREAKER = "circuitBreakerForKafka";

    private final static String RATELIMIT_MARKREADY_API = "markReadinessRateLimit";

    @PostMapping("/driverRegistration")
   @CircuitBreaker(name = KAFKA_CIRCUIT_BREAKER, fallbackMethod = "fallBackForUserSubscription")
    public ResponseEntity<?> registerDriverDetails(
            @RequestBody DriverRegistrationDTO driverRegistrationDTO) throws UserAlreadyExistsException{

        DriverSubscriptionResponse savedDriverDetails = null;
             savedDriverDetails = driverUserDetailsService.saveUniqueDriverDetailsToDB(driverRegistrationDTO);

        try {
            kafkaProducer.sendMessage(KAFKA_TOPIC, savedDriverDetails);
        }
        catch (Exception exception){
            DriverSubscriptionResponse driverSubscriptionResponse = driverUserDetailsService.deleteDriverFromDB(driverRegistrationDTO);
            String serverUnavailableMessage = String.format("There seems to be a problem from our side, " +
                    "please try saving details for %s again", driverSubscriptionResponse.getEmail());
            throw new KafkaConnectionException(serverUnavailableMessage);
        }

        return ResponseEntity.ok(savedDriverDetails);
    }
    public ResponseEntity<?> fallBackForUserSubscription(KafkaConnectionException e){
        String circuitBreakerMessage = String.format("Our servers seems to be down. Please give us sometime " +
                "while we are trying to fix it. Comeback later and try registering.");
        throw new KafkaConnectionException(circuitBreakerMessage);
    }

    @PostMapping("/driverAuthentication")
    public ResponseEntity<?> createAuthenticationToken
            (@RequestBody AuthenticationRequest authenticationRequest) {
        UserDetails userDetails = null;
        userDetails = driverUserDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                            authenticationRequest.getPassword()));

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @PatchMapping("/charioteer/{username}")
    @RateLimiter(name = RATELIMIT_MARKREADY_API
          , fallbackMethod = "fallBackForMarkingReadyRateLimitation")
    public ResponseEntity<?> charioteerReadiness
            (@RequestHeader String authorization,
             @PathVariable String username,@Valid @RequestBody DriverReadinessDTO driverReadinessDTO)
            throws DriverNotVerifiedException , ExpiredJwtException, MalformedJwtException, Exception{
        final String jwtToken = authorization.substring(7);
        final UserDetails userDetails = driverUserDetailsService
                .loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwtToken, userDetails)){
           return ResponseEntity.badRequest().body("The token was not valid. Try authenticating once again");
        }
        DriverVerifiedAndAvailableDetailsResponse driverVerifiedAndAvailableDetailsResponse = null;
            driverVerifiedAndAvailableDetailsResponse =
                    driverUserDetailsService.updateDriverReadinessToTakeRide(userDetails, driverReadinessDTO.isReady());
        if(driverReadinessDTO.isReady()) {
            return ResponseEntity.ok(String.format("%s is now ready to take a ride request", driverVerifiedAndAvailableDetailsResponse.getEmail()));
        }
        else {
            return ResponseEntity.ok(String.format("%s is marked unavailable to take a ride request", driverVerifiedAndAvailableDetailsResponse.getEmail()));
        }
    }
    public ResponseEntity<?> fallBackForMarkingReadyRateLimitation(RequestNotPermitted exception){
        String markReadyAfterFewSeconds = String.format("Perhaps you tried to mark ready too many times " +
                "that could yield undesired result. Please wait for sometime and mark your availability carefully");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(markReadyAfterFewSeconds);
    }
}
