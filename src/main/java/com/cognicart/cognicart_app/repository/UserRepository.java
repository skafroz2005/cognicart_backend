package com.cognicart.cognicart_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cognicart.cognicart_app.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail(String email);

}