package com.app.shoppy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.shoppy.adapter.ProductImageAdapter
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.ActivityProductDetailBinding
import com.google.android.material.chip.Chip

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var repository: StyleNestRepository
    private var selectedSize: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StyleNestRepository(this)
        
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val product = repository.getProductById(productId)

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvProductName.text = product.name
        binding.tvProductCategory.text = "${product.category} > ${product.subCategory}"
        binding.tvProductPrice.text = String.format("KSh %.2f", product.price)
        binding.tvRichDescription.text = product.richDescription

        // Heart Icon Logic
        val isFav = repository.isFavorite(product.id)
        if (isFav) {
            binding.btnFavoriteDetail.setColorFilter(android.graphics.Color.RED)
        } else {
            binding.btnFavoriteDetail.setColorFilter(getColor(R.color.matte_grey))
        }

        binding.btnFavoriteDetail.setOnClickListener {
            val nowFav = repository.toggleWishlist(product.id)
            if (nowFav) {
                binding.btnFavoriteDetail.setColorFilter(android.graphics.Color.RED)
            } else {
                binding.btnFavoriteDetail.setColorFilter(getColor(R.color.matte_grey))
            }
        }

        // Setup Image Slideshow
        val imagesList = product.images.split(",")
        val imageAdapter = ProductImageAdapter(imagesList)
        binding.vpProductImages.adapter = imageAdapter

        // Setup Size Chips
        val sizes = product.sizes.split(",")
        for ((index, size) in sizes.withIndex()) {
            val chip = Chip(this).apply {
                id = android.view.View.generateViewId()
                text = size.trim()
                isCheckable = true
                isClickable = true
                chipBackgroundColor = getColorStateList(R.color.matte_grey)
                setTextColor(getColor(android.R.color.white))
            }
            binding.cgSizes.addView(chip)
            
            if (index == 0) {
                binding.cgSizes.check(chip.id)
                selectedSize = chip.text.toString()
            }
        }

        binding.cgSizes.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                selectedSize = chip.text.toString()
            } else {
                selectedSize = null
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnAddToCart.setOnClickListener {
            if (selectedSize == null) {
                Toast.makeText(this, "Please select a size first", Toast.LENGTH_SHORT).show()
            } else {
                repository.addToCart(product.id, selectedSize!!, 1)
                Toast.makeText(this, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
