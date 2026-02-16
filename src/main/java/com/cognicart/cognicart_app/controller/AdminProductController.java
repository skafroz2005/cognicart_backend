package com.cognicart.cognicart_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.request.CreateProductRequest;
import com.cognicart.cognicart_app.response.ApiResponse;
import com.cognicart.cognicart_app.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest req) {
        Product product = productService.createProduct(req);
        return new ResponseEntity<Product>(product, HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) throws ProductException {
        productService.deleteProduct(productId);
        ApiResponse res = new ApiResponse("Product deleted successfully", true);
        return new ResponseEntity<ApiResponse>(res, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> findAllProduct() {
        // This endpoint might be used for admin dashboards to see raw lists
        // Ideally, filtering logic is in the user controller, but simple list here:
        return new ResponseEntity<>(null, HttpStatus.OK); // Placeholder if not strictly defined
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<Product> updateProduct(@RequestBody Product req, @PathVariable Long productId) throws ProductException {
        Product product = productService.updateProduct(productId, req);
        return new ResponseEntity<Product>(product, HttpStatus.CREATED);
    }

    @PostMapping("/creates")
    public ResponseEntity<ApiResponse> createMultipleProduct(@RequestBody CreateProductRequest[] req) {
        for(CreateProductRequest product:req) {
            productService.createProduct(product);
        }
        ApiResponse res = new ApiResponse("products created successfully", true);
        return new ResponseEntity<ApiResponse>(res, HttpStatus.CREATED);
    }
}