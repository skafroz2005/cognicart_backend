package com.cognicart.cognicart_app.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.request.CreateProductRequest;

public interface ProductService {

    public Product createProduct(CreateProductRequest req);

    public String deleteProduct(Long productId) throws ProductException;

    public Product updateProduct(Long productId, Product req) throws ProductException;

    public Product findProductById(Long id) throws ProductException;

    public List<Product> findProductByCategory(String category);

    public Page<Product> getAllProduct(String category, List<String> colors, List<String> sizes, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber, Integer pageSize);

}