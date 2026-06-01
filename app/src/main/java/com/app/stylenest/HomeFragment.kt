package com.app.stylenest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.stylenest.adapter.CategoryAdapter
import com.app.stylenest.adapter.ProductAdapter
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

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
        setupCategories()
        setupProducts()
    }

    private fun setupCategories() {
        val categories = listOf("All", "Shirts", "Trousers", "Dresses", "Shoes", "Accessories")
        val categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            if (selectedCategory == "All") {
                productAdapter.updateData(repository.getFeaturedProducts())
            } else {
                productAdapter.updateData(repository.getProductsByCategory(selectedCategory))
            }
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter
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
                val defaultSize = product.sizes.split(",").firstOrNull() ?: "One Size"
                repository.addToCart(product.id, defaultSize, 1)
                Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(context, 2)
        binding.rvProducts.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
