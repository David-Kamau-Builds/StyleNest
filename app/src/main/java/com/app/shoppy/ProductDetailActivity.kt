package com.app.shoppy

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.ActivityProductDetailBinding
import com.app.shoppy.model.Product
import com.google.android.material.chip.Chip

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var repository: StyleNestRepository
    private var currentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StyleNestRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        if (productId != -1) {
            loadProductData(productId)
        }

        binding.btnAddToCart.setOnClickListener {
            val product = currentProduct ?: return@setOnClickListener
            
            val selectedChipId = binding.sizeChipGroup.checkedChipId
            if (selectedChipId == View.NO_ID) {
                Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val selectedChip = findViewById<Chip>(selectedChipId)
            val selectedSize = selectedChip.text.toString()

            repository.addToCart(product.id, selectedSize, 1)
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
            finish() // go back to browsing
        }
    }

    private fun loadProductData(productId: Int) {
        val product = repository.getAllProducts().find { it.id == productId }
        product?.let {
            currentProduct = it
            binding.detailName.text = it.name
            binding.detailPrice.text = "KSh ${it.price}"
            binding.detailDescription.text = it.description

            val resourceId = resources.getIdentifier(it.imageName, "mipmap", packageName)
            if(resourceId != 0) {
                binding.detailImage.setImageResource(resourceId)
            }

            // Populate Sizes
            val sizes = it.sizes.split(",")
            sizes.forEach { size ->
                val chip = Chip(this).apply {
                    text = size.trim()
                    isCheckable = true
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#1A1A1A"))
                    setTextColor(Color.WHITE)
                    checkedIconTint = ColorStateList.valueOf(Color.BLACK)
                }
                
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) {
                        chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#D4AF37")) // Gold
                        chip.setTextColor(Color.BLACK)
                    } else {
                        chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#1A1A1A")) // Matte Grey
                        chip.setTextColor(Color.WHITE)
                    }
                }
                
                binding.sizeChipGroup.addView(chip)
            }
            
            if (binding.sizeChipGroup.childCount > 0) {
                (binding.sizeChipGroup.getChildAt(0) as Chip).isChecked = true
            }
        }
    }
}
