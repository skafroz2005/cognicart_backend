package com.cognicart.cognicart_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognicart.cognicart_app.exception.UserException;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

//    public UserController(UserService userService) {
//        this.userService = userService;
//    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String jwt) throws UserException {
        // You can optionally verify the JWT here to ensure only Admins can access this list
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.ACCEPTED);
    }
}