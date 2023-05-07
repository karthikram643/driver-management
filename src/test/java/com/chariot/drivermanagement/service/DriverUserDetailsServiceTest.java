package com.chariot.drivermanagement.service;

import com.chariot.drivermanagement.dto.DriverRegistrationDTO;
import com.chariot.drivermanagement.exception.DriverNotVerifiedException;
import com.chariot.drivermanagement.exception.UserAlreadyExistsException;
import com.chariot.drivermanagement.model.*;
import com.chariot.drivermanagement.repository.UserDynamoRepository;
import com.chariot.drivermanagement.util.TestUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class DriverUserDetailsServiceTest {

    @InjectMocks
    private DriverUserDetailsService driverUserDetailsService;

    @Mock
    private UserDynamoRepository userDynamoRepository;

    private final static String TESTUSER_EMAIL = "testuser@gmail.com";

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Test
    void loadUserByUsernameWithCorrectEmail() {
        User user = TestUtil.fetchTestUser();

        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(user);
        UserDetails userDetails = driverUserDetailsService.loadUserByUsername(TESTUSER_EMAIL);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals(TESTUSER_EMAIL,user.getEmail());
    }

    @Test
    void loadUserByUsernameWithADifferentCaseEmail() {
        String emailDifferentCase = "tesTUser@gmAil.com";
        User user = TestUtil.fetchTestUser();

        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(user);
        UserDetails userDetails = driverUserDetailsService.loadUserByUsername(emailDifferentCase);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals(emailDifferentCase.toLowerCase(),user.getEmail());
    }

    @Test
    void testUserNameNotFoundException() {
        User user = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,() -> driverUserDetailsService.loadUserByUsername(TESTUSER_EMAIL));
    }

    @Test
    void testDtoToUserConversionVerifyAllFieldValues() {
        String password = "passwordEncrypted";

        Mockito.when(bCryptPasswordEncoder.encode(Mockito.anyString())).thenReturn(password);

        DriverRegistrationDTO userToBeSaved = TestUtil.fetchDriverRegistrationDTOForTest();
        User user = driverUserDetailsService.convertToUserAndSaveToDB(userToBeSaved);
        Assert.assertEquals(userToBeSaved.getFirstName(),user.getFirstName());
        Assert.assertEquals(userToBeSaved.getLastName(),user.getLastName());
        Assert.assertEquals(userToBeSaved.getEmail(),user.getEmail());
        Assert.assertEquals(userToBeSaved.getDriverLicenseNumber(),user.getDriverLicenseNumber());
        Assert.assertEquals(Role.DRIVER, user.getRole());

        /** Passwords do not match since the password is encoded **/
        Assert.assertNotNull(userToBeSaved.getPassword(),user.getPassword());

    }

    @Test
    void testSavingDuplicateDriverToDBAndExpectAnException() {
        User userInDB = TestUtil.fetchTestUser();

        DriverRegistrationDTO driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);

        assertThrows(UserAlreadyExistsException.class,() -> driverUserDetailsService.saveUniqueDriverDetailsToDB(driverRegistrationDTO));
    }

    @Test
    void testSavingNewDriverToDBAndExpectSuccessfulResponseDTO() throws UserAlreadyExistsException {
        User savedUserInDB = TestUtil.fetchTestUser();
        String password = "passwordEncrypted";
        DriverRegistrationDTO driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(savedUserInDB);
        Mockito.when(bCryptPasswordEncoder.encode(Mockito.anyString())).thenReturn(password);



        DriverSubscriptionResponse savedUserToDB = driverUserDetailsService.saveUniqueDriverDetailsToDB(driverRegistrationDTO);
        Assert.assertNotNull(savedUserToDB);
        Assert.assertEquals(driverRegistrationDTO.getFirstName(),savedUserToDB.getFirstName());
        Assert.assertNotNull(driverRegistrationDTO.getLastName(),savedUserToDB.getLastName());
        Assert.assertNotNull(driverRegistrationDTO.getEmail(),savedUserToDB.getEmail());
        Assert.assertNotNull(driverRegistrationDTO.getDriverLicenseNumber(),savedUserToDB.getDriverLicenseNumber());

    }


    @Test
    void testEnrichEmailAddressByLowerCasing() {
        String testEmail = "TesTUser@gMail.Com";
        String testEmailWhenLowerCased = "testuser@gmail.com";
        String lowerCaseEmail = driverUserDetailsService.enrichEmailAddressByLowerCasing(testEmail);
        Assert.assertEquals(testEmailWhenLowerCased,lowerCaseEmail);

    }

    @Test
    void testEnrichEmailAddressByLowerCasingWhenEmailIsNull() {
        String testNullEmailWhenLowerCased = "";
        String lowerCaseEmail = driverUserDetailsService.enrichEmailAddressByLowerCasing(null);
        Assert.assertEquals(testNullEmailWhenLowerCased,lowerCaseEmail);

    }

    @Test
    void testDeleteAnExistingDriverUserFromDB() throws UserAlreadyExistsException {
        DriverRegistrationDTO driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();
        User userFromDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.deleteUser(Mockito.anyString())).thenReturn(userFromDB);

        DriverSubscriptionResponse driverSubscriptionResponse = driverUserDetailsService.deleteDriverFromDB(driverRegistrationDTO);
        Assert.assertNotNull(driverSubscriptionResponse);
        Assert.assertNotNull(userFromDB.getEmail(),driverSubscriptionResponse.getEmail());
        Assert.assertNotNull(userFromDB.getFirstName(),driverSubscriptionResponse.getFirstName());
        Assert.assertNotNull(userFromDB.getLastName(),driverSubscriptionResponse.getLastName());
        Assert.assertNotNull(userFromDB.getDriverLicenseNumber(),driverSubscriptionResponse.getDriverLicenseNumber());
    }

    @Test
    void testDeletingANonExistingExistingDriverUserFromDB() {
        DriverRegistrationDTO driverRegistrationDTO = TestUtil.fetchDriverRegistrationDTOForTest();
        Mockito.when(userDynamoRepository.deleteUser(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,() -> driverUserDetailsService.deleteDriverFromDB(driverRegistrationDTO));
    }

    @Test
    void testUpdateDriverReadinessToTrue() throws DriverNotVerifiedException {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        userInDB.setVerified(true);
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(userInDB);


        DriverVerifiedAndAvailableDetailsResponse driverVerifiedResponse = driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails, true);
        Assert.assertNotNull(driverVerifiedResponse);
        Assert.assertTrue(driverVerifiedResponse.isAvailable());
    }

    @Test
    void testUpdateDriverReadinessToFalse() throws DriverNotVerifiedException {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        userInDB.setVerified(true);
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(userInDB);


        DriverVerifiedAndAvailableDetailsResponse driverVerifiedResponse = driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails, false);
        Assert.assertNotNull(driverVerifiedResponse);
        Assert.assertFalse(driverVerifiedResponse.isAvailable());
    }

    @Test
    void testUpdatingDriverReadinessToTrueBeforeVerificationThatThrowsException() {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(userInDB);

        assertThrows(DriverNotVerifiedException.class, () -> driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails,true));

//        DriverVerifiedAndAvailableDetailsResponse driverVerifiedResponse = driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails, false);
//        Assert.assertNotNull(driverVerifiedResponse);
//        Assert.assertFalse(driverVerifiedResponse.isAvailable());
    }

    @Test
    void testUpdatingDriverReadinessToFalseBeforeVerificationThatThrowsException() {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(userInDB);

        assertThrows(DriverNotVerifiedException.class, () -> driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails,false));
    }

    @Test
    void testUpdatingDriverReadinessToTrueForANonExistentUserInDB() {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails,true));
    }

    @Test
    void testUpdatingDriverReadinessToFalseForANonExistentUserInDB() {
        DriverUserDetails driverUserDetails = TestUtil.fetchDriverUserDetailsForTest();
        User userInDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> driverUserDetailsService.updateDriverReadinessToTakeRide(driverUserDetails,false));
    }

    @Test
    void testUpdateDriverVerificationForInvalidEmail() {
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> driverUserDetailsService.updateDriverVerifiedInTheSystem(TESTUSER_EMAIL,true));


    }

    @Test
    void testUpdateDriverVerificationForValidUser() {
        User userInDB = TestUtil.fetchTestUser();
        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(userInDB);
        Mockito.when(userDynamoRepository.save(Mockito.any(User.class))).thenReturn(userInDB);


        DriverVerifiedAndAvailableDetailsResponse driverVerifiedResponse =
                driverUserDetailsService.updateDriverVerifiedInTheSystem(TESTUSER_EMAIL, true);

        Assert.assertNotNull(driverVerifiedResponse);
        Assert.assertTrue(driverVerifiedResponse.isVerified());


    }
}