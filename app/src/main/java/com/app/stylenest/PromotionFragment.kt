package com.app.stylenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.app.stylenest.adapter.ProductAdapter
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.FragmentPromotionBinding

class PromotionFragment : Fragment() {
    private var _binding: FragmentPromotionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPromotionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = StyleNestRepository(requireContext())
        val promoType = arguments?.getString("PROMO_TYPE") ?: "NEW"

        if (promoType == "NEW") {
            binding.tvPromoTitle.text = "New Arrivals"
        } else {
            binding.tvPromoTitle.text = "50% Off Sale"
        }

        binding.btnBack.setOnClickListener { 
            parentFragmentManager.popBackStack()
        }

        val allProducts = repository.getAllProducts()
        val filteredProducts = if (promoType == "NEW") {
            allProducts.sortedByDescending { it.id }.take(10)
        } else {
            allProducts.filter { it.id % 2 == 0 }
        }

        if (filteredProducts.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = if (promoType == "NEW") "No new arrivals yet" else "No 50% off items at the moment."
            binding.rvPromoProducts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvPromoProducts.visibility = View.VISIBLE
            
            productAdapter = ProductAdapter(
                products = filteredProducts,
                promoType = promoType,
                onProductClick = { product ->
                    val intent = android.content.Intent(requireContext(), ProductDetailActivity::class.java)
                    intent.putExtra("PRODUCT_ID", product.id)
                    startActivity(intent)
                },
                onQuickAddClick = { product ->
                    val sizesArray = product.sizes.split(",").map { it.trim() }.toTypedArray()
                    val finalPrice = if (promoType == "SALE") product.price * 0.5 else product.price
                    if (sizesArray.isEmpty() || sizesArray[0].isEmpty()) {
                        repository.addToCartWithPrice(product.id, "One Size", 1, finalPrice)
                        Toast.makeText(requireContext(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.updateBadges()
                    } else {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Select Size")
                            .setItems(sizesArray) { _, which ->
                                val selectedSize = sizesArray[which]
                                repository.addToCartWithPrice(product.id, selectedSize, 1, finalPrice)
                                Toast.makeText(requireContext(), "${product.name} (Size: $selectedSize) added to cart!", Toast.LENGTH_SHORT).show()
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
            binding.rvPromoProducts.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.rvPromoProducts.adapter = productAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
