package com.chariot.drivermanagement.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


@Data
@Builder
@AllArgsConstructor
@DynamoDBTable(tableName = "user")
@DynamoDBDocument
public class User {

    @DynamoDBAttribute
    @JsonProperty(required = true)
    private String firstName;

    @DynamoDBAttribute
    @JsonProperty(required = true)
    private String lastName;

    @NonNull
    @DynamoDBHashKey(attributeName= "email")
    @JsonProperty(required = true)
    private String email;

    @DynamoDBAttribute
    @JsonProperty(required = true)
    private String password;

    @NonNull
    @DynamoDBAttribute
    @JsonProperty(required = true)
    private String driverLicenseNumber;

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "roles")
    private Role role;

    @DynamoDBAttribute
    private boolean isAvailable;

    @DynamoDBAttribute
    private boolean isVerified;


    public User() {

    }

    public User(String firstName, String lastName, String email, String password,String driverLicenseNumber, Role role) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.driverLicenseNumber = driverLicenseNumber;
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "roles")
    public Role getRole() {
        return role;
    }

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "roles")
    public void setRole(Role role) {
        this.role = role;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(@NonNull String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", driverLicenseNumber='" + driverLicenseNumber + '\'' +
                ", role=" + role +
                ", isAvailable=" + isAvailable +
                ", isVerified=" + isVerified +
                '}';
    }
}

