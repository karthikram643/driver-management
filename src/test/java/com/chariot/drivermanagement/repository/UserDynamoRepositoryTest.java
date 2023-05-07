//package com.chariot.drivermanagement.repository;
//
//import com.chariot.drivermanagement.model.User;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.security.RunAs;
//
//@RunWith(SpringRunner.class)
//@DataJpaTest
//class UserDynamoRepositoryTest {
//
//    @Autowired
//    private UserDynamoRepository userDynamoRepository;
//
//
//    @Test
//    void save() {
//        User userToBeSaved = User.builder()
//                .email("karthik@gmail.com")
//                .build();
//        User savedUser = userDynamoRepository.save(userToBeSaved);
//        Assertions.assertThat(savedUser).isNotNull();
//        Assertions.assertThat(savedUser.getEmail()).isEqualTo("karthik@gmail.com");
//
//    }
//
//    @Test
//    void findByEmail() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
//}