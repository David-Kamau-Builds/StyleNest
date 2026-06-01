package com.app.stylenest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.stylenest.databinding.ItemProductBinding
import com.app.stylenest.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onQuickAddClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        with(holder.binding) {
            productName.text = product.name
            productCategory.text = product.category
            productPrice.text = "KSh ${product.price}"
            
            // Offline image loading: dynamically find our drawable/mipmap by name
            val context = root.context
            val resourceId = context.resources.getIdentifier(product.imageName, "mipmap", context.packageName)
            if(resourceId != 0) {
                productImage.setImageResource(resourceId)
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
}
