package com.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public ResponseEntity createUser(@RequestBody UserCreateRequest userCreateRequest) throws JsonProcessingException {
          userService.create(userCreateRequest);
          return new ResponseEntity("user Created successfully", HttpStatus.CREATED );
    }

    @GetMapping("/user")
    public User getUserDetails(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        User user=(User) authentication.getPrincipal();
        return  userService.loadUserByUsername(user.getPhoneNumber());
    }

    @GetMapping("/admin/all/users")
    public List<User>  getAllUserDetails(){
        return  userService.getAllUserDetails();
    }


    @GetMapping("/admin/all/{userId}")
    public User  getAllUserDetails(@PathVariable String userId){
        return  userService.loadUserByUsername(userId);
    }

}
