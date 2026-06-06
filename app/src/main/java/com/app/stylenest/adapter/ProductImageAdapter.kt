package com.app.stylenest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.stylenest.databinding.ItemProductImageBinding

class ProductImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemProductImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.binding.root.context)
            .load(images[position])
            .centerCrop()
            .placeholder(com.app.stylenest.utils.UIUtils.getShimmerDrawable())
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int = images.size
}
