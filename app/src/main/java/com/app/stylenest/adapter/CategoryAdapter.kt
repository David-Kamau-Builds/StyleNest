package com.app.stylenest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.stylenest.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            categoryName.text = category
            
            if (position == selectedPosition) {
                // Active state: Gold background, Black text
                root.setCardBackgroundColor(Color.parseColor("#D4AF37")) // Gold
                categoryName.setTextColor(Color.BLACK)
            } else {
                // Inactive state: Matte Grey background, White text
                root.setCardBackgroundColor(Color.parseColor("#1A1A1A")) // Matte Grey
                categoryName.setTextColor(Color.WHITE)
            }

            root.setOnClickListener {
                val previousPos = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPos)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category)
            }
        }
    }

    override fun getItemCount(): Int = categories.size
}
