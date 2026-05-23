package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.shoppy.databinding.ItemProductBinding
import com.app.shoppy.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val promoType: String? = null,
    private val onProductClick: (Product) -> Unit,
    private val onQuickAddClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private var favoriteIds: Set<Long> = emptySet()
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateProducts(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavorites: Set<Long>) {
        this.favoriteIds = newFavorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        with(holder.binding) {
            productName.text = product.name
            productCategory.text = product.category
            
            // Apply Promotional Logic
            if (promoType == "NEW") {
                tvBadge.visibility = android.view.View.VISIBLE
                tvBadge.text = "NEW"
                tvBadge.setBackgroundColor(root.context.getColor(com.app.shoppy.R.color.black))
                tvOriginalPrice.visibility = android.view.View.GONE
                productPrice.text = String.format("KSh %.2f", product.price)
            } else if (promoType == "SALE") {
                tvBadge.visibility = android.view.View.VISIBLE
                tvBadge.text = "-50%"
                tvBadge.setBackgroundColor(android.graphics.Color.RED)
                tvOriginalPrice.visibility = android.view.View.VISIBLE
                tvOriginalPrice.text = String.format("KSh %.2f", product.price)
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                productPrice.text = String.format("KSh %.2f", product.price * 0.5)
            } else {
                tvBadge.visibility = android.view.View.GONE
                tvOriginalPrice.visibility = android.view.View.GONE
                productPrice.text = String.format("KSh %.2f", product.price)
            }
            
            Glide.with(root.context)
                .load(product.imageUrl)
                .centerCrop()
                .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
                .into(productImage)
            
            // Handle Heart UI State dynamically
            val isFav = favoriteIds.contains(product.id.toLong())
            if (isFav) {
                ivFavorite.setColorFilter(android.graphics.Color.RED)
            } else {
                val matteGrey = root.context.getColor(com.app.shoppy.R.color.matte_grey)
                ivFavorite.setColorFilter(matteGrey)
            }
            
            ivFavorite.setOnClickListener {
                onFavoriteClick(product)
                notifyItemChanged(position)
            }
            
            root.setOnClickListener { onProductClick(product) }
            btnQuickAdd.setOnClickListener { onQuickAddClick(product) }
        }
    }

    override fun getItemCount(): Int = products.size
    
    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    fun addData(newProducts: List<Product>) {
        val startPosition = products.size
        products = products + newProducts
        notifyItemRangeInserted(startPosition, newProducts.size)
    }
}

