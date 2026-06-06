package com.app.stylenest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.stylenest.adapter.ProductImageAdapter
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.ActivityProductDetailBinding
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

        // Resolve a theme-aware "off" colour for the heart so it looks right in
        // both light and dark mode — instead of hardcoding matte_grey.
        val heartOffColor = com.google.android.material.color.MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnSurface, android.graphics.Color.GRAY
        )

        // Heart Icon Logic
        fun applyHeartTint(isFav: Boolean) {
            val color = if (isFav) android.graphics.Color.RED else heartOffColor
            binding.btnFavoriteDetail.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        applyHeartTint(repository.isFavorite(product.id))

        binding.btnFavoriteDetail.setOnClickListener {
            val nowFav = repository.toggleWishlist(product.id)
            applyHeartTint(nowFav)
            val msg = if (nowFav) "Added to wishlist!" else "Removed from wishlist"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Setup Image Slideshow
        val imagesList = product.images.split(",")
        val imageAdapter = ProductImageAdapter(imagesList)
        binding.vpProductImages.adapter = imageAdapter

        // Setup Size Chips — do NOT set chipBackgroundColor or text colour manually.
        // Material Chip manages its own checked/unchecked state colours automatically,
        // giving correct adaptive colours + a visible ripple on tap.
        val sizes = product.sizes.split(",")
        for ((index, size) in sizes.withIndex()) {
            val chip = Chip(this).apply {
                id = android.view.View.generateViewId()
                text = size.trim()
                isCheckable = true
                isClickable = true
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
