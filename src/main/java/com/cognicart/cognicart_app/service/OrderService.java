package com.cognicart.cognicart_app.service;

import java.util.List;

import com.cognicart.cognicart_app.exception.OrderException;
import com.cognicart.cognicart_app.model.Address;
import com.cognicart.cognicart_app.model.Order;
import com.cognicart.cognicart_app.model.User;

public interface OrderService {

    public Order createOrder(User user, Address shippingAddress);

    public Order findOrderById(Long orderId) throws OrderException;

    public List<Order> usersOrderHistory(Long userId);

    public Order placedOrder(Long orderId) throws OrderException;

    public Order confirmedOrder(Long orderId) throws OrderException;

    public Order shippedOrder(Long orderId) throws OrderException;

    public Order deliveredOrder(Long orderId) throws OrderException;

    public Order cancleOrder(Long orderId) throws OrderException;

    public void deleteOrder(Long orderId) throws OrderException;

}