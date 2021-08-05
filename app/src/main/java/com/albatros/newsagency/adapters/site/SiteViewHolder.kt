package com.albatros.newsagency.adapters.site

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.R
import com.albatros.newsagency.Site
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.databinding.SiteLayoutBinding
import com.squareup.picasso.Picasso

class SiteViewHolder(private val binding : SiteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    private fun picIntoViewByLink(link: String, view: ImageView) {
        val context: Context = binding.root.context
        context.assets
        view.setImageResource(R.drawable.rss_icon)
        try {
           Picasso.with(context).load(link).error(R.drawable.rss_icon).into(view)
        } catch (e: Exception) {
            view.setImageResource(R.drawable.rss_icon)
        }
        Log.d(link, "!!!")
    }


    fun bind(site: Site) {
        binding.siteDataView.text = binding.root.context.getString(R.string.str_site_data, site.name, site.url)
            .replace(".xml".toRegex(), "")
            .replace("://".toRegex(), "")
            .replace("https".toRegex(), "")
            .replace("http".toRegex(), "")
            .replace("ftp".toRegex(), "")
            .replace("ftps".toRegex(), "")
        val count = RssItemManager.newsList.count {
            it.site.name == site.name
        }
        val plural: String = binding.root.context.resources.getQuantityString(
            R.plurals.items_plurals,
            count
        )
        binding.quantData.text =
            binding.root.context.getString(R.string.str_items_count, count, plural)
        picIntoViewByLink(site.imageLink, binding.icon)
    }
}