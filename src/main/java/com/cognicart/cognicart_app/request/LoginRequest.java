package com.cognicart.cognicart_app.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {
        // TODO Auto-generated constructor stub
    }

}