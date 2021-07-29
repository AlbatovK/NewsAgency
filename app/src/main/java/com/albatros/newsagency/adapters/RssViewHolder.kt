package com.albatros.newsagency.adapters

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.RssItem
import com.albatros.newsagency.databinding.ItemLayoutBinding

class RssViewHolder(private val binding: ItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: RssItem) {
        binding.titleTxt.text = item.title + "\nИсточник : " + item.site.name
        binding.dateTxt.text = item.date.toString().subSequence(0, 16)
        binding.root.setOnClickListener {
            val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
            binding.root.context.startActivity(actionIntent)
        }
    }
}

