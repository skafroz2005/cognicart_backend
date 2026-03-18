package com.cognicart.cognicart_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Category;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.repository.CategoryRepository;
import com.cognicart.cognicart_app.repository.ProductRepository;
import com.cognicart.cognicart_app.request.CreateProductRequest;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private UserService userService;
    private CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserService userService, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product createProduct(CreateProductRequest req) {

        Category topLevel = categoryRepository.findByName(req.getTopLevelCategory());
        if(topLevel == null) {
            Category topLavelCategory = new Category();
            topLavelCategory.setName(req.getTopLevelCategory());
            topLavelCategory.setLevel(1);
            topLevel = categoryRepository.save(topLavelCategory);
        }

        Category secondLevel = categoryRepository.findByNameAndParent(req.getSecondLevelCategory(), topLevel.getName());
        if(secondLevel == null) {
            Category secondLavelCategory = new Category();
            secondLavelCategory.setName(req.getSecondLevelCategory());
            secondLavelCategory.setParentCategory(topLevel);
            secondLavelCategory.setLevel(2);
            secondLevel = categoryRepository.save(secondLavelCategory);
        }

        Category thirdLevel = categoryRepository.findByNameAndParent(req.getThirdLevelCategory(), secondLevel.getName());
        if(thirdLevel == null) {
            Category thirdLavelCategory = new Category();
            thirdLavelCategory.setName(req.getThirdLevelCategory());
            thirdLavelCategory.setParentCategory(secondLevel);
            thirdLavelCategory.setLevel(3);
            thirdLevel = categoryRepository.save(thirdLavelCategory);
        }

        Product product = new Product();
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());

        product.setTags(req.getTags());
        product.setDescription(req.getDescription());
        product.setDiscountedPrice(req.getDiscountedPrice());
        product.setDiscountPercent(req.getDiscountPercent());
        product.setImageUrl(req.getImageUrl());

        product.setImages(req.getImages());

        product.setBrand(req.getBrand());
        product.setPrice(req.getPrice());
        product.setSizes(req.getSize());
        product.setQuantity(req.getQuantity());
        product.setCategory(thirdLevel);
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Override
    public String deleteProduct(Long productId) throws ProductException {
        Product product = findProductById(productId);
        product.getSizes().clear();
        productRepository.delete(product);
        return "Product deleted successfully";
    }

    @Override
    public Product updateProduct(Long productId, Product req) throws ProductException {
        Product product = findProductById(productId);
        if(req.getQuantity() != 0) {
            product.setQuantity(req.getQuantity());
        }
        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long id) throws ProductException {
        Optional<Product> opt = productRepository.findById(id);
        if(opt.isPresent()) {
            return opt.get();
        }
        throw new ProductException("Product not found with id - " + id);
    }

    @Override
    public List<Product> findProductByCategory(String category) {
        // Logic to be implemented if needed, or use repository directly
        return null;
    }

    @Override
    public Page<Product> getAllProduct(String category, String topLevelCategory, String searchQuery, List<String> colors, List<String> sizes, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber, Integer pageSize) {

        // --- APPLY THE PLURAL HACK HERE ---
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            searchQuery = searchQuery.toLowerCase().trim();
            if (searchQuery.endsWith("ies")) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 3);
            } else if (searchQuery.endsWith("es") && !searchQuery.endsWith("shoes")) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 2);
            } else if (searchQuery.endsWith("s") && !searchQuery.endsWith("ss")) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // 1. Fetch data WITHOUT the sort parameter
        List<Product> products = productRepository.filterProducts(category, topLevelCategory, searchQuery, minPrice, maxPrice, minDiscount);
        // REPLACE IT WITH THIS NEW CODE:

        // 2. Add the Java Sorting Logic right here!
        // 2. Sort in Java (Optimized for primitive 'int')
        if (sort != null && !sort.isEmpty()) {
            if (sort.equalsIgnoreCase("price_low")) {
                // Low to High
                products.sort((p1, p2) -> Integer.compare(p1.getDiscountedPrice(), p2.getDiscountedPrice()));
            } else if (sort.equalsIgnoreCase("price_high")) {
                // High to Low
                products.sort((p1, p2) -> Integer.compare(p2.getDiscountedPrice(), p1.getDiscountedPrice()));
            }
        }

        if (colors != null && !colors.isEmpty()) {
            products = products.stream()
                    .filter(p -> {
                        // Safety check: skip if the product has no color saved
                        if (p.getColor() == null) return false;

                        // Partial Match Logic: Check if the product's color contains any of the selected filter colors
                        return colors.stream().anyMatch(c ->
                                p.getColor().toLowerCase().contains(c.toLowerCase())
                        );
                    })
                    .collect(Collectors.toList());
        }

        if(stock != null) {
            if(stock.equals("in_stock")) {
                products = products.stream().filter(p -> p.getQuantity() > 0).collect(Collectors.toList());
            } else if (stock.equals("out_of_stock")) {
                products = products.stream().filter(p -> p.getQuantity() < 1).collect(Collectors.toList());
            }
        }

        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min((startIndex + pageable.getPageSize()), products.size());

        List<Product> pageContent = products.subList(startIndex, endIndex);

        Page<Product> filteredProducts = new PageImpl<>(pageContent, pageable, products.size());

        return filteredProducts;
    }

    @Override
    public List<Product> findAllProducts() {
        return List.of();
    }

    @Override
    public List<Product> searchProduct(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Clean the string
        String searchTerm = query.toLowerCase().trim();

        // Basic English Stemming (Plural Fix)
        if (searchTerm.endsWith("ies")) {
            // changes "accessories" to "accessor" (which matches accessory and accessories)
            searchTerm = searchTerm.substring(0, searchTerm.length() - 3);
        } else if (searchTerm.endsWith("es") && !searchTerm.endsWith("shoes")) {
            // changes "watches" to "watch"
            searchTerm = searchTerm.substring(0, searchTerm.length() - 2);
        } else if (searchTerm.endsWith("s") && !searchTerm.endsWith("ss")) {
            // changes "shirts" to "shirt"
            searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
        }

        // Pass the optimized word to the database
        return productRepository.searchProduct(searchTerm);
    }
}