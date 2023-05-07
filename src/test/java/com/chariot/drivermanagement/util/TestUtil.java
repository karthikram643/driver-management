package com.chariot.drivermanagement.util;

import com.chariot.drivermanagement.dto.DriverReadinessDTO;
import com.chariot.drivermanagement.dto.DriverRegistrationDTO;
import com.chariot.drivermanagement.model.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {


    public static User fetchTestUser(){
        return User.builder()
                .firstName("Test").lastName("user")
                .email("testuser@gmail.com")
                .driverLicenseNumber("TestLicense")
                .build();
    }

    public static DriverRegistrationDTO fetchDriverRegistrationDTOForTest(){
        return DriverRegistrationDTO.builder()
                .firstName("Test").lastName("user")
                .email("testuser@gmail.com")
                .password("testPassword")
                .driverLicenseNumber("TestLicense")
                .build();
    }

    public static DriverUserDetails fetchDriverUserDetailsForTest(){
        return DriverUserDetails.builder()
                .user(fetchTestUser())
                .build();
    }

    public static DriverReadinessDTO fetchDriverReadinessDTO(boolean readiness){
        return DriverReadinessDTO.builder()
                .isReady(readiness)
                .build();
    }

    public static DriverSubscriptionResponse fetchDriverSubscriptionResponse(){
        User subscribedUser = fetchTestUser();
        return DriverSubscriptionResponse.builder()
                .firstName(subscribedUser.getFirstName())
                .lastName(subscribedUser.getLastName())
                .email(subscribedUser.getEmail())
                .driverLicenseNumber(subscribedUser.getDriverLicenseNumber())
                .build();
    }


    public static DriverVerifiedAndAvailableDetailsResponse fetchDriverVerifiedAndAvailableDetailsResponseForTest(){
        User verifiedAndAvailableUser = fetchTestUser();
        return DriverVerifiedAndAvailableDetailsResponse.builder()
                .firstName(verifiedAndAvailableUser.getFirstName())
                .lastName(verifiedAndAvailableUser.getLastName())
                .email(verifiedAndAvailableUser.getEmail())
                .driverLicenseNumber(verifiedAndAvailableUser.getDriverLicenseNumber())
                .available(true)
                .verified(true)
                .build();
    }

    public static AuthenticationRequest fetchAuthenticationRequestForValidTestUser(){
        return AuthenticationRequest.builder()
                .username(fetchTestUser().getEmail())
                .password(fetchTestUser().getPassword())
                .build();
    }

    public static AuthenticationRequest fetchAuthenticationRequestForUnAuthenticatedTestUser(){
        return AuthenticationRequest.builder()
                .username(fetchTestUser().getEmail())
                .password("InvalidPassword")
                .build();
    }
    public static AuthenticationRequest fetchAuthenticationRequestForInvalidTestUser(){
        return AuthenticationRequest.builder()
                .username("invalidUser@gmail.com")
                .password("InvalidPassword")
                .build();
    }





}
