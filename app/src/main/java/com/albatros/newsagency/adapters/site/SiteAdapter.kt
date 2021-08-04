package com.albatros.newsagency.adapters.site

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.RssItem
import com.albatros.newsagency.Site
import com.albatros.newsagency.adapters.rss.RssViewHolder
import com.albatros.newsagency.databinding.ItemLayoutBinding
import com.albatros.newsagency.databinding.SiteLayoutBinding

class SiteAdapter(private val items: List<Site>) : RecyclerView.Adapter<SiteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val binding = SiteLayoutBinding.inflate(LayoutInflater.from(parent.context))
        return SiteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

}