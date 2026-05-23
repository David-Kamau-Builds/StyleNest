package com.app.shoppybackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String avatarUrl;

    private Boolean twoFactorEnabled;
    
    // Feature: Loyalty Points System
    private Integer loyaltyPoints = 0;
}
