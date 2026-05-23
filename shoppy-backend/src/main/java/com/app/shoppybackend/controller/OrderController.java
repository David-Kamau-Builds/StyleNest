package com.app.shoppybackend.controller;

import com.app.shoppybackend.entity.Order;
import com.app.shoppybackend.entity.AppUser;
import com.app.shoppybackend.repository.OrderRepository;
import com.app.shoppybackend.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AppUserRepository userRepository;

    @GetMapping("/user/{email}")
    public List<Order> getUserOrders(@PathVariable String email) {
        return orderRepository.findByUserEmailOrderByIdDesc(email);
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // Initialize default tracking status
        order.setStatus("Pending Delivery");
        Order savedOrder = orderRepository.save(order);
        
        // Feature: Loyalty Points System - Award 1 point per 100 KSh spent
        if (order.getUserEmail() != null) {
            userRepository.findByEmail(order.getUserEmail()).ifPresent(user -> {
                int pointsEarned = (int) (order.getTotalAmount() / 100);
                user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsEarned);
                userRepository.save(user);
            });
        }
        
        return ResponseEntity.ok(savedOrder);
    }
    
    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Feature: Admin endpoint to update order tracking status
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String orderNumber, @RequestParam String status) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order != null) {
            order.setStatus(status);
            return ResponseEntity.ok(orderRepository.save(order));
        }
        return ResponseEntity.notFound().build();
    }
}
