package com.springmongotests.demo.service;

import com.mongodb.client.result.UpdateResult;
import com.springmongotests.demo.data.User;
import com.springmongotests.demo.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    @NonNull
    private MongoOperations mongoTemplate;

    @NonNull UserRepository userRepository;

    public List<User> findUserByHobby(String hobbyName){
        return userRepository.findByHobbiesHobbyName(hobbyName);
    }

    public Optional<User> findUserById(String Id){
        return userRepository.findById(Id);
    }

    public UpdateResult removeHobbyEntry(String hobbyName){
        Query removeQuery = Query.query(Criteria.where("id").is("user1"));
        Update removeUpdate = new Update().pull("hobbies", Query.query(Criteria.where("hobbyName").is(hobbyName)));
        UpdateResult updateResult =
                mongoTemplate.updateMulti(removeQuery, removeUpdate, User.class);
        return updateResult;
    }
}