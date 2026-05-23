package com.app.shoppybackend.controller;

import com.app.shoppybackend.entity.AppUser;
import com.app.shoppybackend.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private AppUserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        // Initialize loyalty points
        user.setLoyaltyPoints(0);
        return ResponseEntity.ok(userRepository.save(user));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        return userRepository.findByEmailAndPassword(email, password)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
    
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserProfile(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateUserProfile(@PathVariable String email, @RequestBody AppUser updatedUser) {
        return userRepository.findByEmail(email).map(user -> {
            if (updatedUser.getName() != null) user.setName(updatedUser.getName());
            if (updatedUser.getAvatarUrl() != null) user.setAvatarUrl(updatedUser.getAvatarUrl());
            if (updatedUser.getTwoFactorEnabled() != null) user.setTwoFactorEnabled(updatedUser.getTwoFactorEnabled());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}
