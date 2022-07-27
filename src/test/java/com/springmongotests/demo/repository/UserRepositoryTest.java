package com.springmongotests.demo.repository;

import com.mongodb.client.result.UpdateResult;
import com.springmongotests.demo.data.Hobby;
import com.springmongotests.demo.data.User;
import com.springmongotests.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@Slf4j
class UserRepositoryTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoOperations mongoTemplate;

    @BeforeAll
    @DisplayName("Start Mongo Db container")
    static void startMongoDbContainer() {
        mongoDBContainer.start();
        log.info("Test Container Started");
    }

//    @AfterAll
//    @DisplayName("Stop Mongo Db container")
//    static void stopMongoDbContainer() throws InterruptedException {
//        Thread.sleep(5000);
//        mongoDBContainer.stop();
//        log.info("Test Container Stopped");
//
//    }

    @AfterEach
    void cleanUp() {
        this.userRepository.deleteAll();
    }

    @Test
    void shouldReturnListOfUserWithMatchingAge() {
        this.userRepository.save(new User("user1", 42, Collections.emptyList()));
        this.userRepository.save(new User("user2", 55, Collections.emptyList()));

        List<User> customers = userRepository.findByAgeBetween(40, 56);

        assertEquals(2, customers.size());
        log.info("shouldReturnListOfCustomerWithMatchingRate successful");
    }

    @Test
    void shouldReturnUserWithMatchingHobby() {
        UserService service = new UserService(mongoTemplate, userRepository);
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3))));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3))));

        List<User> customers = userRepository.findByHobbiesHobbyName("Food");

        assertEquals(2, customers.size());

        customers = userRepository.findByHobbiesHobbyName("Hiking");

        assertEquals(1, customers.size());
    }

    @Test
    void shouldDeleteEntryWithMatchingHobby() {
        UserService service = new UserService(mongoTemplate, userRepository);
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3))));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3))));

        UpdateResult updateResult = service.removeHobbyEntry("Biking");
        User user1 = service.findUserById("user1").orElse(null);

        log.info("{}", Map.of("updateResult", updateResult, "user1", user1));
        assertEquals(1, user1.getHobbies().size());

    }
}