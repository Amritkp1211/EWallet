package com.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.CommanConstant;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.user.UserConstants.USER_AUTHORITY;
import static com.wallet.CommanConstant.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public void create(UserCreateRequest userCreateRequest) throws JsonProcessingException {
         User user= userCreateRequest.toUser();
         user.setPassword(passwordEncoder.encode(user.getPassword()));
         user.setAuthorities(USER_AUTHORITY);
        user= userRepository.save(user);

        //publish the event post user creation which will be listened by consumers
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(USER_CREATED_TOPIC_USERID,user.getId());
        jsonObject.put(USER_CREATED_TOPIC_PHONE_NUMBER,user.getPhoneNumber());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_KEY,user.getUserIdentifier());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_VALUE,user.getIdentifierValue());
        jsonObject.put(USER_CREATED_TOPIC_EMAIL,user.getEmail());

        kafkaTemplate.send(USER_CREATED_TOPIC,objectMapper.writeValueAsString(jsonObject));


    }

    @Override
    public User loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
      User user=  userRepository.findByPhoneNumber( phoneNumber);
        return user;
    }

    public List<User> getAllUserDetails() {
        return userRepository.findAll();

    };
}
