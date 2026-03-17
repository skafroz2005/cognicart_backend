package com.cognicart.cognicart_app.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cognicart.cognicart_app.model.Size;

public class CreateProductRequest {

    private String title;
    private String description;
    private int price;
    private int discountedPrice;

    private int discountPercent;
    private int quantity;
    private String brand;
    private String color;
    private Set<Size> size = new HashSet<>();
    private String imageUrl;

    private List<String> images = new ArrayList<>(); //Added by me

    // Remove: private String tags;
    private List<String> tags;

    // Kept "Lavel" typo to match video implementation
    private String topLevelCategory;
    private String secondLevelCategory;
    private String thirdLevelCategory;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public int getDiscountedPrice() {
        return discountedPrice;
    }
    public void setDiscountedPrice(int discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getTopLevelCategory() {
        return topLevelCategory;
    }

    public String getSecondLevelCategory() {
        return secondLevelCategory;
    }

    public String getThirdLevelCategory() {
        return thirdLevelCategory;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setTopLevelCategory(String topLevelCategory) {
        this.topLevelCategory = topLevelCategory;
    }

    public void setSecondLevelCategory(String secondLevelCategory) {
        this.secondLevelCategory = secondLevelCategory;
    }

    public void setThirdLevelCategory(String thirdLevelCategory) {
        this.thirdLevelCategory = thirdLevelCategory;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public Set<Size> getSize() {
        return size;
    }
    public void setSize(Set<Size> size) {
        this.size = size;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getImages() {
        return images;
    }


}