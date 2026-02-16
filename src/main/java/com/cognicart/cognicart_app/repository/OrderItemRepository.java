package com.cognicart.cognicart_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cognicart.cognicart_app.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}