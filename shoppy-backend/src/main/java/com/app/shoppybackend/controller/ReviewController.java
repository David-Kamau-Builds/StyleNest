package com.app.shoppybackend.controller;

import com.app.shoppybackend.entity.Review;
import com.app.shoppybackend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/product/{productId}")
    public List<Review> getProductReviews(@PathVariable Long productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        String dateString = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        review.setDate(dateString);
        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
