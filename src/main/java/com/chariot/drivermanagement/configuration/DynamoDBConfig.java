package com.chariot.drivermanagement.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfig {

    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDBEndpoint;
    @Value("${aws.dynamodb.region}")
    private String dynamoDBRegion;
    @Value("${aws.dynamodb.accessKey}")
    public  String dynamoDBAccessKey;
    @Value("${aws.dynamodb.secretKey}")
    public  String dynamoDBSecretKey;

    @Bean
    public DynamoDBMapper mapper(){
        return new DynamoDBMapper(amazonDynamoDBConfig());
    }

    private AmazonDynamoDB amazonDynamoDBConfig(){

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        (new AwsClientBuilder
                                .EndpointConfiguration(
                                dynamoDBEndpoint, dynamoDBRegion)))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(dynamoDBAccessKey, dynamoDBSecretKey)))
                .build();
    }
}
