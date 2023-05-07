package com.chariot.drivermanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverVerifiedAndAvailableDetailsResponse {

    private String firstName;

    private String lastName;

    private String email;

    private String driverLicenseNumber;

    private boolean available;

    private boolean verified;
}
