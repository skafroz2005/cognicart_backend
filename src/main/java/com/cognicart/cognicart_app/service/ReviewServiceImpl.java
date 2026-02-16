package com.cognicart.cognicart_app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.model.Review;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.repository.ProductRepository;
import com.cognicart.cognicart_app.repository.ReviewRepository;
import com.cognicart.cognicart_app.request.ReviewRequest;

@Service
public class ReviewServiceImpl implements ReviewService {

    private ReviewRepository reviewRepository;
    private ProductService productService;
    private ProductRepository productRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ProductService productService, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Override
    public Review createReview(ReviewRequest req, User user) throws ProductException {
        Product product = productService.findProductById(req.getProductId());

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReview(req.getReview());
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getAllReview(Long productId) {
        return reviewRepository.getAllProductsReview(productId);
    }

}