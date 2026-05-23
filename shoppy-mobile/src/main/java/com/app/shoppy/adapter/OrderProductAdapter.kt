package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.shoppy.databinding.ItemOrderProductBinding
import com.app.shoppy.data.remote.model.OrderItemDto
import com.app.shoppy.ui.viewmodel.SharedProductViewModel

class OrderProductAdapter(
    private var items: List<OrderItemDto>,
    private val productViewModel: SharedProductViewModel
) : RecyclerView.Adapter<OrderProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvProductSize.text = "Size: ${item.size}"
            tvQuantity.text = "Qty: ${item.quantity}"
            tvProductPrice.text = String.format("KSh %.2f", item.price * item.quantity)
            
            // Try to find the product name from the SharedProductViewModel
            val product = productViewModel.products.value.firstOrNull { it.id == item.productId }
            if (product != null) {
                tvProductName.text = product.name
            } else {
                tvProductName.text = "Product #${item.productId}"
            }
        }
    }

    override fun getItemCount(): Int = items.size
    
    fun updateData(newItems: List<OrderItemDto>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
