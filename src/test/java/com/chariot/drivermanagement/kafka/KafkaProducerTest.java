package com.chariot.drivermanagement.kafka;

import com.chariot.drivermanagement.exception.KafkaConnectionException;
import com.chariot.drivermanagement.exception.UserAlreadyExistsException;
import com.chariot.drivermanagement.model.DriverSubscriptionResponse;
import com.chariot.drivermanagement.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class KafkaProducerTest {

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @Mock
    private KafkaTemplate kafkaTemplate;


    @Test
    void testSendMessageToKafka(){
        String testKafkaTopic = "test-driver-subscription";
        DriverSubscriptionResponse driverSubscriptionResponse =
                TestUtil.fetchDriverSubscriptionResponse();

        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any())).thenReturn(null);
        kafkaProducer.sendMessage(testKafkaTopic,driverSubscriptionResponse);

        Mockito.verify(kafkaTemplate,Mockito.times(1))
                .send(testKafkaTopic,driverSubscriptionResponse.toString());
    }

    @Test
    void testSendMessageToKafka_And_ExpectException(){
        String testKafkaTopic = "test-driver-subscription";
        DriverSubscriptionResponse driverSubscriptionResponse =
                TestUtil.fetchDriverSubscriptionResponse();
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any())).thenThrow(KafkaConnectionException.class);

        assertThrows(KafkaConnectionException.class,
                () -> kafkaProducer.sendMessage(testKafkaTopic,driverSubscriptionResponse));
    }
}