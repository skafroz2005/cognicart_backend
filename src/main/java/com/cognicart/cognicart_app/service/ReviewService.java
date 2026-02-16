package com.cognicart.cognicart_app.service;

import java.util.List;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Review;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.request.ReviewRequest;

public interface ReviewService {

    public Review createReview(ReviewRequest req, User user) throws ProductException;

    public List<Review> getAllReview(Long productId);

}