package com.chariot.drivermanagement.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.chariot.drivermanagement.model.Role;
import com.chariot.drivermanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserDynamoRepository {

    @Autowired
    private DynamoDBMapper mapper;

    public User save(User user){
        mapper.save(user);
        return user;
    }

    public User findByEmail(String email){
        User user = mapper.load(User.class,email);
        return user;
    }

    public User deleteUser(String email){
        User user =findByEmail(email);
        mapper.delete(user);
        return user;
    }
}
