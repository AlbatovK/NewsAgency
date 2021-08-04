package com.albatros.newsagency.adapters.site

import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.R
import com.albatros.newsagency.Site
import com.albatros.newsagency.databinding.SiteLayoutBinding

class SiteViewHolder(private val binding : SiteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(site: Site) {
        binding.img.setImageResource(R.drawable.rss_icon)
        binding.siteNameTxt.text = site.name
    }
}