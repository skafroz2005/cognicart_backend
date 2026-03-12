package com.cognicart.cognicart_app.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
    private String payment_link_url;
    private String payment_link_id;

    // Generate empty constructor, all-args constructor, getters, and setters here
}