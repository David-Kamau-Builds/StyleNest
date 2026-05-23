package com.app.shoppybackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    
    private String orderNumber;
    
    private String customerName;
    
    private String phone;
    
    private String deliveryAddress;
    
    private String paymentMethod;
    
    private Double totalAmount;
    
    private String orderDate;
    
    // Feature: Order Tracking Pipeline
    // e.g. Pending Delivery -> Packed -> Shipped -> Delivered
    private String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();
}
