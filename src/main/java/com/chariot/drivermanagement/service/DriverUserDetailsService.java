package com.chariot.drivermanagement.service;

import com.chariot.drivermanagement.dto.DriverRegistrationDTO;
import com.chariot.drivermanagement.exception.DriverNotVerifiedException;
import com.chariot.drivermanagement.exception.UserAlreadyExistsException;
import com.chariot.drivermanagement.model.*;
import com.chariot.drivermanagement.repository.UserDynamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class DriverUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDynamoRepository userDynamoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /**
         * converting to lowercase as email addresses are not case-sensitive
         * **/
        String email = enrichEmailAddressByLowerCasing(username);
        
        User user = userDynamoRepository.findByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException(String.format("User with email %s was not found in the system. " +
                    "Please check the email address once again",email));
        }
        user.setRole(Role.DRIVER);

        return new DriverUserDetails(user);

    }

    public DriverSubscriptionResponse saveUniqueDriverDetailsToDB(DriverRegistrationDTO driverRegistrationDTO) throws UserAlreadyExistsException {
        String email = enrichEmailAddressByLowerCasing(driverRegistrationDTO.getEmail());
        User userInDB = userDynamoRepository.findByEmail(email);
        if(null == userInDB) {
            User userToBeSaved = convertToUserAndSaveToDB(driverRegistrationDTO);
            User savedUser = userDynamoRepository.save(userToBeSaved);

            return buildDriverSubscriptionResponse(savedUser);
        }
        else{
            throw new UserAlreadyExistsException(String.format("Driver with email Id %s already exists in the system." +
                    "Please sign up with a different email address",driverRegistrationDTO.getEmail()));
        }
    }

    public DriverSubscriptionResponse deleteDriverFromDB(DriverRegistrationDTO driverRegistrationDTO) throws UsernameNotFoundException, UserAlreadyExistsException {
        String email = enrichEmailAddressByLowerCasing(driverRegistrationDTO.getEmail());
        User userDeleteFromInDB = userDynamoRepository.deleteUser(email);
        if(null == userDeleteFromInDB) {
            throw new UsernameNotFoundException(String.format("User with email %s was not found in the system. " +
                    "Please check the email address once again", email));
        }

            return buildDriverSubscriptionResponse(userDeleteFromInDB);
    }
    public DriverVerifiedAndAvailableDetailsResponse updateDriverReadinessToTakeRide(
            UserDetails userDetails,Boolean isReady)
            throws DriverNotVerifiedException, UsernameNotFoundException{
        User userInDB = userDynamoRepository.findByEmail(userDetails.getUsername());
        if(null == userInDB) {
            throw new UsernameNotFoundException("Driver doesn't exist in the system");
        }
        if(!userInDB.isVerified()) {
          throw new DriverNotVerifiedException();
        }
        userInDB.setAvailable(isReady);
        User updatedUser = userDynamoRepository.save(userInDB);

        return buildDriverVerifiedAndAvailableDetailsResponse(updatedUser);
    }

    public DriverVerifiedAndAvailableDetailsResponse updateDriverVerifiedInTheSystem(String username,boolean isVerified)
            throws UsernameNotFoundException{
          String email = enrichEmailAddressByLowerCasing(username);
        User userInDB = userDynamoRepository.findByEmail(email);
        if(null == userInDB) {
            throw new UsernameNotFoundException(String.format("Driver %s doesn't exist in the records",email));
        }
        userInDB.setVerified(isVerified);
        User updatedUser = userDynamoRepository.save(userInDB);

        return buildDriverVerifiedAndAvailableDetailsResponse(updatedUser);
    }



    protected static DriverSubscriptionResponse buildDriverSubscriptionResponse(User savedUser) {
        return DriverSubscriptionResponse.builder().firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .driverLicenseNumber(savedUser.getDriverLicenseNumber())
                .build();
    }

    protected static DriverVerifiedAndAvailableDetailsResponse buildDriverVerifiedAndAvailableDetailsResponse(User savedUser) {
        return DriverVerifiedAndAvailableDetailsResponse.builder().firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .driverLicenseNumber(savedUser.getDriverLicenseNumber())
                .available(savedUser.isAvailable())
                .verified(savedUser.isVerified())
                .build();
    }

    public String enrichEmailAddressByLowerCasing(String email) {
        if(null != email)
       return email.toLowerCase();
        return "";
    }
    
    protected User convertToUserAndSaveToDB(DriverRegistrationDTO driverRegistrationDTO){
        User userToBeSaved = new User(driverRegistrationDTO.getFirstName(),
                driverRegistrationDTO.getLastName(),
                /**
                 * converting to lowercase as email addresses are not case-sensitive
                 * **/
                driverRegistrationDTO.getEmail().toLowerCase(),
                passwordEncoder.encode(driverRegistrationDTO.getPassword()),
                driverRegistrationDTO.getDriverLicenseNumber(),
                Role.DRIVER);
        
        return userToBeSaved;
    }
}
