package com.cognicart.cognicart_app.service;

import java.util.List;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Rating;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.request.RatingRequest;

public interface RatingService {

    public Rating createRating(RatingRequest req, User user) throws ProductException;

    public List<Rating> getProductsRating(Long productId);

}