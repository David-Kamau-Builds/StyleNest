package com.app.stylenest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.app.stylenest.adapter.ProductAdapter
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.FragmentWishlistBinding

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = StyleNestRepository(requireContext())
        setupWishlist()
    }

    private fun setupWishlist() {
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                val defaultSize = product.sizes.split(",").firstOrNull() ?: "One Size"
                repository.addToCart(product.id, defaultSize, 1)
                Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.updateBadges()
            },
            onFavoriteClick = { product ->
                repository.toggleWishlist(product.id)
                (activity as? MainActivity)?.updateBadges()
                loadWishlist() // Reload the list to remove it if unfavorited
            },
            isFavorite = { productId ->
                repository.isFavorite(productId)
            }
        )
        binding.rvWishlist.layoutManager = GridLayoutManager(context, 2)
        binding.rvWishlist.adapter = productAdapter
        loadWishlist()
    }

    private fun loadWishlist() {
        val items = repository.getWishlistItems()
        if (items.isEmpty()) {
            binding.rvWishlist.visibility = View.GONE
            binding.tvEmptyWishlist.visibility = View.VISIBLE
        } else {
            binding.rvWishlist.visibility = View.VISIBLE
            binding.tvEmptyWishlist.visibility = View.GONE
            productAdapter.updateData(items)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::productAdapter.isInitialized) {
            loadWishlist()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
