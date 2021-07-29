package com.albatros.newsagency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.RssItem
import com.albatros.newsagency.databinding.ItemLayoutBinding

class RssAdapter(private val items: List<RssItem>) : RecyclerView.Adapter<RssViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context))
        return RssViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RssViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

}