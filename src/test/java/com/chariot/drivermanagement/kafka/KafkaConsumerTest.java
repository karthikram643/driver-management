package com.chariot.drivermanagement.kafka;

import com.chariot.drivermanagement.model.DriverVerifiedAndAvailableDetailsResponse;
import com.chariot.drivermanagement.repository.UserDynamoRepository;
import com.chariot.drivermanagement.service.DriverUserDetailsService;
import com.chariot.drivermanagement.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaConsumerTest {

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Mock
    private DriverUserDetailsService userDetailsService;

    @Mock
    private UserDynamoRepository userDynamoRepository;


    @Test
    void testConsume_For_ValidUserInDB() {
        String kafkaMessage = TestUtil.fetchTestUser().getEmail();
        DriverVerifiedAndAvailableDetailsResponse verifiedResponse = TestUtil.fetchDriverVerifiedAndAvailableDetailsResponseForTest();
        Mockito.when(userDetailsService.updateDriverVerifiedInTheSystem(
                Mockito.anyString(), Mockito.anyBoolean()))
                .thenReturn(verifiedResponse);

        kafkaConsumer.consume(kafkaMessage);
        Mockito.verify(userDetailsService,Mockito.times(1))
                .updateDriverVerifiedInTheSystem(kafkaMessage,true);

    }

//    @Test
//    void testConsume_For_InValidUserInDB(){
//        String kafkaMessage = "invalidUser@gmail.com";
//        Mockito.when(userDetailsService.updateDriverVerifiedInTheSystem(
//                        Mockito.anyString(), Mockito.anyBoolean()))
//                .thenReturn(null);
//        Mockito.when(userDynamoRepository.findByEmail(Mockito.anyString())).thenReturn(null);
//
//        Assertions.assertThrows(UsernameNotFoundException.class , () -> kafkaConsumer.consume(kafkaMessage));
//
//    }
}
