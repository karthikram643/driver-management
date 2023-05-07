package com.chariot.drivermanagement.kafka;

import com.chariot.drivermanagement.exception.KafkaConnectionException;
import com.chariot.drivermanagement.model.DriverSubscriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);


    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic,DriverSubscriptionResponse driverSubscriptionResponse) throws KafkaConnectionException{
        LOGGER.info(String.format("Event published for user : %s",driverSubscriptionResponse.getEmail()));
        try {
            kafkaTemplate.send(topic, driverSubscriptionResponse.toString());
        }catch(Exception exception){
            String serverUnavailableMessage = String.format("There seems to be a problem from our side, " +
                    "please try saving details for %s again", driverSubscriptionResponse.getEmail());
            throw new KafkaConnectionException(serverUnavailableMessage);
        }
    }
}
