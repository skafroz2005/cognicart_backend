package com.cognicart.cognicart_app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="order_id")
    private String orderId;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    @OneToOne
    private Address shippingAddress;

    @Embedded
    private PaymentDetails paymentDetails = new PaymentDetails();

    private double totalPrice;
    private Integer totalDiscountedPrice;
    private Integer discount;
    private String orderStatus;
    private int totalItem;
    private LocalDateTime createdAt;

    public Order() {
    }

//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getOrderId() { return orderId; }
//    public void setOrderId(String orderId) { this.orderId = orderId; }
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//    public List<OrderItem> getOrderItems() { return orderItems; }
//    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
//    public LocalDateTime getOrderDate() { return orderDate; }
//    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
//    public LocalDateTime getDeliveryDate() { return deliveryDate; }
//    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
//    public Address getShippingAddress() { return shippingAddress; }
//    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
//    public PaymentDetails getPaymentDetails() { return paymentDetails; }
//    public void setPaymentDetails(PaymentDetails paymentDetails) { this.paymentDetails = paymentDetails; }
//    public double getTotalPrice() { return totalPrice; }
//    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
//    public Integer getTotalDiscountedPrice() { return totalDiscountedPrice; }
//    public void setTotalDiscountedPrice(Integer totalDiscountedPrice) { this.totalDiscountedPrice = totalDiscountedPrice; }
//    public Integer getDiscounte() { return discount; }
//    public void setDiscounte(Integer discounte) { this.discount = discounte; }
//    public String getOrderStatus() { return orderStatus; }
//    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
//    public int getTotalItem() { return totalItem; }
//    public void setTotalItem(int totalItem) { this.totalItem = totalItem; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}