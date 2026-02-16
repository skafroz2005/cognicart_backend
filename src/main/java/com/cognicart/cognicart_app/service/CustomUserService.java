package com.cognicart.cognicart_app.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserService {

    public UserDetails loadUserByUsername(String username);
}
