package com.cognicart.cognicart_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cognicart.cognicart_app.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {


//    @Query("SELECT DISTINCT p FROM Product p " +
//            "LEFT JOIN p.tags t " + // <--- 1. ADD THE JOIN HERE
//            "WHERE (:category IS NULL OR :category = '' OR p.category.name = :category) " +
//            "AND (:topLevelCategory IS NULL OR :topLevelCategory = '' OR p.category.parentCategory.parentCategory.name = :topLevelCategory) " +
//            "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
//            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
//            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
//            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
//            "LOWER(t) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " + // <--- 2. USE 't' INSTEAD OF 'p.tags'
//            "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR (p.discountedPrice BETWEEN :minPrice AND :maxPrice)) " +
//            "AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount) " +
//            "ORDER BY " +
//            "CASE WHEN :sort = 'price_low' THEN p.discountedPrice END ASC, " +
//            "CASE WHEN :sort = 'price_high' THEN p.discountedPrice END DESC")
//    public List<Product> filterProducts(
//            @Param("category") String category,
//            @Param("topLevelCategory") String topLevelCategory,
//            @Param("searchQuery") String searchQuery,
//            @Param("minPrice") Integer minPrice,
//            @Param("maxPrice") Integer maxPrice,
//            @Param("minDiscount") Integer minDiscount,
//            @Param("sort") String sort
//    );

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.tags t " +
            "WHERE (:category IS NULL OR :category = '' OR p.category.name = :category) " +
            "AND (:topLevelCategory IS NULL OR :topLevelCategory = '' OR p.category.parentCategory.parentCategory.name = :topLevelCategory) " +
            "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
            "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR (p.discountedPrice BETWEEN :minPrice AND :maxPrice)) " +
            "AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount)")
    public List<Product> filterProducts(
            @Param("category") String category,
            @Param("topLevelCategory") String topLevelCategory,
            @Param("searchQuery") String searchQuery,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount
    ); // Notice we removed the @Param("sort") from here!




//    @Query("SELECT p FROM Product p WHERE " +
//            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
//            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
//            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
//            "LOWER(p.color) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
//            "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
//            "LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%'))") // <--- ADDED THIS LINE
//    public List<Product> searchProduct(@Param("query") String query);


    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.tags t " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.color) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))")
    public List<Product> searchProduct(@Param("query") String query);
}