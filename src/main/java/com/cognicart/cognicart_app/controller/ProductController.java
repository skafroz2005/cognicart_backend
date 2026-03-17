package com.cognicart.cognicart_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<Page<Product>> findProductByCategoryHandler(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String topLevelCategory,
            @RequestParam(required = false) String searchQuery, // <--- ADD THIS LINE
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) List<String> size,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {

        // If pageNumber or pageSize arrive null from the frontend, set defaults to prevent crashes
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 10;

        Page<Product> res = productService.getAllProduct(category, topLevelCategory, searchQuery, color, size, minPrice, maxPrice, minDiscount, sort, stock, pageNumber, pageSize);

        System.out.println("Page content: " + res.getContent());
        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @GetMapping("/products/id/{productId}")
    public ResponseEntity<Product> findProductByIdHandler(@PathVariable Long productId) throws ProductException {
        Product product = productService.findProductById(productId);
        return new ResponseEntity<Product>(product, HttpStatus.ACCEPTED);
    }


//    @GetMapping("/products/search")
//    public ResponseEntity<List<Product>> searchProductHandler(@RequestParam String q) {
//        List<Product> products = productService.searchProduct(q);
//        return new ResponseEntity<>(products, HttpStatus.OK);
//    }

}