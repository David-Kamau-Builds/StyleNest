package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

    data class BentoSlide(val title: String, val imageUrl: String)
    private val bentoSlides = listOf(
        BentoSlide("Urban\nWear", "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Street\nStyle", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Autumn\nVibes", "https://images.unsplash.com/photo-1550614000-4b95d466f288?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Evening\nElegance", "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?q=80&w=600&auto=format&fit=crop")
    )
    private var currentSlideIndex = 0
    private val slideHandler = Handler(Looper.getMainLooper())
    private lateinit var slideRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = StyleNestRepository(requireContext())
        
        // Load static small bento images
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=400&auto=format&fit=crop")
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoTopRight)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=400&auto=format&fit=crop")
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoBottomRight)
            
        binding.cardNewArrivals.setOnClickListener {
            val fragment = PromotionFragment().apply {
                arguments = Bundle().apply {
                    putString("PROMO_TYPE", "NEW")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding.cardSale.setOnClickListener {
            val fragment = PromotionFragment().apply {
                arguments = Bundle().apply {
                    putString("PROMO_TYPE", "SALE")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        startBentoSlideshow()
        setupProducts()
    }

    private fun startBentoSlideshow() {
        slideRunnable = object : Runnable {
            override fun run() {
                currentSlideIndex = (currentSlideIndex + 1) % bentoSlides.size
                val slide = bentoSlides[currentSlideIndex]
                
                // Animate text fade out/in
                binding.tvBentoTitle.animate().alpha(0f).setDuration(400).withEndAction {
                    if (_binding != null) {
                        binding.tvBentoTitle.text = slide.title
                        binding.tvBentoTitle.animate().alpha(1f).setDuration(400).start()
                    }
                }.start()

                // Load next image with crossfade
                if (_binding != null) {
                    Glide.with(this@HomeFragment)
                        .load(slide.imageUrl)
                        .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
                        .transition(DrawableTransitionOptions.withCrossFade(800))
                        .into(binding.ivBentoMain)
                }
                    
                slideHandler.postDelayed(this, 5000) // 5 seconds per slide
            }
        }
        
        // Load initial slide immediately
        val initialSlide = bentoSlides[0]
        binding.tvBentoTitle.text = initialSlide.title
        Glide.with(this)
            .load(initialSlide.imageUrl)
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoMain)
            
        slideHandler.postDelayed(slideRunnable, 5000)
    }

    private fun setupProducts() {
        val initialProducts = repository.getFeaturedProducts()
        productAdapter = ProductAdapter(
            products = initialProducts,
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                val sizesArray = product.sizes.split(",").map { it.trim() }.toTypedArray()
                if (sizesArray.isEmpty() || sizesArray[0].isEmpty()) {
                    repository.addToCart(product.id, "One Size", 1)
                    Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.updateBadges()
                } else {
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Select Size")
                        .setItems(sizesArray) { _, which ->
                            val selectedSize = sizesArray[which]
                            repository.addToCart(product.id, selectedSize, 1)
                            Toast.makeText(context, "${product.name} (Size: $selectedSize) added to cart!", Toast.LENGTH_SHORT).show()
                            (activity as? MainActivity)?.updateBadges()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onFavoriteClick = { product ->
                repository.toggleWishlist(product.id)
                (activity as? MainActivity)?.updateBadges()
            },
            isFavorite = { productId ->
                repository.isFavorite(productId)
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(context, 2)
        binding.rvProducts.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::slideRunnable.isInitialized) {
            slideHandler.removeCallbacks(slideRunnable)
        }
        _binding = null
    }
}
