package com.app.shoppybackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    
    private String userEmail;
    
    private String userName;
    
    private Integer rating; // 1 to 5
    
    @Column(length = 1000)
    private String comment;
    
    private String date;
}
