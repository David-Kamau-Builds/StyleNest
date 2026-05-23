package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.shoppy.databinding.ItemCartBinding
import com.app.shoppy.model.CartItem

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        with(holder.binding) {
            cartItemName.text = item.productName
            cartItemSize.text = "Size: ${item.selectedSize}"
            cartItemPrice.text = String.format("KSh %.2f", item.price)
            cartItemQuantity.text = item.quantity.toString()

            Glide.with(root.context)
                .load(item.productImageUrl)
                .centerCrop()
                .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
                .into(cartItemImage)

            btnIncrease.setOnClickListener {
                onQuantityChange(item, item.quantity + 1)
            }
            
            btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    onQuantityChange(item, item.quantity - 1)
                } else {
                    onQuantityChange(item, 0) // Treat 0 as remove item
                }
            }

            root.setOnClickListener {
                val intent = android.content.Intent(root.context, com.app.shoppy.ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", item.productId)
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size
    
    fun updateData(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
