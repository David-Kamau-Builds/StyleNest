package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.CategoryAdapter
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var currentCategory = "All"
    private var minPrice = 0.0
    private var maxPrice = 10000.0
    
    // Pagination state
    private var currentOffset = 0
    private val limit = 10
    private var isLoading = false
    private var isLastPage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = StyleNestRepository(requireContext())
        
        setupDrawer()
        setupCategories()
        setupProducts()
        setupAdvancedFilters()
        
        // Initial load
        resetPaginationAndApply()
    }

    private fun setupAdvancedFilters() {
        binding.priceSlider.values = listOf(0f, 10000f)
        binding.priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            minPrice = values[0].toDouble()
            maxPrice = values[1].toDouble()
            binding.tvPriceRange.text = "KSh ${minPrice.toInt()} - KSh ${maxPrice.toInt()}"
            resetPaginationAndApply()
        }
        
        binding.btnClearFilters.setOnClickListener {
            currentCategory = "All"
            binding.tvCategoriesTitle.text = "Browse Categories"
            binding.priceSlider.values = listOf(0f, 10000f)
            minPrice = 0.0
            maxPrice = 10000.0
            resetPaginationAndApply()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun resetPaginationAndApply() {
        currentOffset = 0
        isLastPage = false
        applyFilters()
    }

    private fun applyFilters() {
        val newBatch = repository.getFilteredProducts(currentCategory, minPrice, maxPrice, limit, currentOffset)
        if (currentOffset == 0) {
            productAdapter.updateData(newBatch)
        } else {
            productAdapter.addData(newBatch)
        }
        
        if (newBatch.size < limit) {
            isLastPage = true
        }
        isLoading = false
    }

    private fun setupDrawer() {
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupCategories() {
        val groupList = listOf("All", "Shirts", "Trousers", "Dresses", "Shoes", "Accessories")
        val childList = mapOf(
            "All" to emptyList(),
            "Shirts" to listOf("Polos", "Tees", "Button-Downs", "Long-Sleeve"),
            "Trousers" to listOf("Jeans", "Chinos", "Sweatpants", "Cargo"),
            "Dresses" to listOf("Maxi", "Midi", "Summer", "Evening"),
            "Shoes" to listOf("Sneakers", "Boots", "Loafers", "Heels"),
            "Accessories" to listOf("Belts", "Socks", "Perfumes", "Watches")
        )
        
        val adapter = com.app.shoppy.adapter.SidebarExpandableAdapter(requireContext(), groupList, childList)
        binding.expandableCategoryList.setAdapter(adapter)
        
        // Handle child (sub-category) click
        binding.expandableCategoryList.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val selectedSubCategory = childList[groupList[groupPosition]]!![childPosition]
            binding.tvCategoriesTitle.text = selectedSubCategory
            currentCategory = selectedSubCategory // Using sub-category as the filter now
            resetPaginationAndApply()
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }
        
        // Handle group (main category) click
        binding.expandableCategoryList.setOnGroupClickListener { _, _, groupPosition, _ ->
            val mainCategory = groupList[groupPosition]
            if (mainCategory == "All") {
                binding.tvCategoriesTitle.text = mainCategory
                currentCategory = mainCategory
                resetPaginationAndApply()
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                return@setOnGroupClickListener true // consume click, no children to expand
            } else {
                // We want main category to filter as well
                binding.tvCategoriesTitle.text = mainCategory
                currentCategory = mainCategory
                resetPaginationAndApply()
                return@setOnGroupClickListener false // allow expansion
            }
        }
    }

    private fun setupProducts() {
        productAdapter = ProductAdapter(
            products = emptyList(), // Start empty, let pagination load it
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
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.rvCategoryProducts.layoutManager = gridLayoutManager
        binding.rvCategoryProducts.adapter = productAdapter
        
        // Infinite Scroll Listener
        binding.rvCategoryProducts.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = gridLayoutManager.childCount
                val totalItemCount = gridLayoutManager.itemCount
                val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isLoading = true
                        currentOffset += limit
                        applyFilters()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
