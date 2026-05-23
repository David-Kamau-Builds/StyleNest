package com.app.shoppybackend.repository;

import com.app.shoppybackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserEmailOrderByIdDesc(String userEmail);
    Order findByOrderNumber(String orderNumber);
}
