package com.app.shoppybackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 2000)
    private String richDescription;

    private String category;
    
    private String subCategory;
    
    private String sizes;
    
    private Double price;
    
    private String imageUrl;
    
    @Column(length = 1000)
    private String images;
}
