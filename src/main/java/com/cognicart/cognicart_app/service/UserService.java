package com.cognicart.cognicart_app.service;

//package com.cognicart.cognicart_app.service;

import com.cognicart.cognicart_app.exception.UserException;
import com.cognicart.cognicart_app.model.User;

import java.util.List;

public interface UserService {

    public User findUserById(Long userId) throws UserException;

    public User findUserProfileByJwt(String jwt) throws UserException;

    public List<User> findAllUsers();

}