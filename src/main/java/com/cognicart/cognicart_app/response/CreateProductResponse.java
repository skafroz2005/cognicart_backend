package com.cognicart.cognicart_app.response;

import com.cognicart.cognicart_app.model.Product;

public class CreateProductResponse {

    private String message;
    private boolean status;
    private Product product;

    public CreateProductResponse() {
    }

    public CreateProductResponse(String message, boolean status, Product product) {
        this.message = message;
        this.status = status;
        this.product = product;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
