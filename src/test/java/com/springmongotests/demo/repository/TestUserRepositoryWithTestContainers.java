package com.springmongotests.demo.repository;

import com.mongodb.client.result.UpdateResult;
import com.springmongotests.demo.data.Hobby;
import com.springmongotests.demo.data.User;
import com.springmongotests.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@Slf4j
class TestUserRepositoryWithTestContainers {
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

    UserService service;

    @BeforeAll
    @DisplayName("Start Mongo Db container")
    static void startMongoDbContainer() {
        mongoDBContainer.start();
        log.info("Test Container Started");
    }

    @BeforeEach
    void setUp() {
        service = new UserService(mongoTemplate, userRepository);
    }

    @AfterEach
    void cleanUp() {
        this.userRepository.deleteAll();
    }

    @Test
    void shouldReturnListOfUserWithMatchingAge() {
        this.userRepository.save(new User("user1", 42, Collections.emptyList(), Collections.emptyList()));
        this.userRepository.save(new User("user2", 55, Collections.emptyList(), Collections.emptyList()));

        List<User> customers = userRepository.findByAgeBetween(40, 56);

        assertEquals(2, customers.size());
        log.info("shouldReturnListOfCustomerWithMatchingRate successful");
    }

    @Test
    void shouldReturnUserWithMatchingHobby() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        List<User> customers = userRepository.findByHobbiesHobbyName("Food");
        assertEquals(2, customers.size());

        customers = userRepository.findByHobbiesHobbyName("Hiking");
        assertEquals(1, customers.size());
    }

    @Test
    void shouldDeleteEntryWithMatchingHobby() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        UpdateResult updateResult = service.removeHobbyEntry("user1", "Biking");
        User user1 = service.findUserById("user1").orElse(null);

        log.info("{}", Map.of("updateResult", updateResult, "user1", user1));
        assertEquals(1, user1.getHobbies().size());
    }

    @Test
    void shouldDeleteEntryWithNumberValue() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        UpdateResult updateResult = service.removeLuckyNumber("user1", 2);
        User user1 = service.findUserById("user1").orElse(null);

        log.info("{}", Map.of("updateResult", updateResult, "user1", user1));
        assertTrue(CollectionUtils.isEqualCollection(List.of(1,3), user1.getLuckyNumbers()));
    }

    @Test
    void shouldDeleteEntryWithNumberValueList() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        UpdateResult updateResult = service.removeLuckyNumbers("user1", new Integer[]{1,2});
        User user1 = service.findUserById("user1").orElse(null);

        log.info("{}", Map.of("updateResult", updateResult, "user1", user1));
        assertTrue(CollectionUtils.isEqualCollection(List.of(3), user1.getLuckyNumbers()));
    }

    @Test
    void updateSpecificHobbyEntryShouldUpdateEntryWithMatchingHobby() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        UpdateResult updateResult = service.updateSpecificHobbyEntry("Biking", 7);
        List<User> users = service.findUserByHobby("Biking");

        assertNotEquals(0, users.size());
        User firstUser = users.get(0);
        log.info("{}", Map.of("updateResult", updateResult, "user1", firstUser));

        assertEquals(7, firstUser.getHobbies().get(1).getPerWeek());
    }

    @Test
    void updateAllHobbyEntriesShouldUpdateAllEntriesInAllDocuments() {
        this.userRepository.save(new User("user1", 42, List.of(new Hobby("Food", 2), new Hobby("Biking", 3)),List.of(1,2,3)));
        this.userRepository.save(new User("user2", 45, List.of(new Hobby("Food", 2), new Hobby("Hiking", 3)), List.of(4,5,6)));

        UpdateResult updateResult = service.updateAllHobbyEntries(7);
        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        users.stream()
                .forEach(user -> Optional.ofNullable(user.getHobbies()).stream().flatMap(list -> list.stream()).forEach(hobby -> assertEquals(7, hobby.getPerWeek())));
    }


}