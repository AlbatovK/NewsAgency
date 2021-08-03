package com.albatros.newsagency.adapters

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.R
import com.albatros.newsagency.RssItem
import com.albatros.newsagency.RssItemManager
import com.albatros.newsagency.databinding.ItemLayoutBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RssViewHolder(private val binding: ItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var item: RssItem? = null
        get() = field!!
        set(value) {
            field = value
            bind(field)
        }

    private fun bind(item: RssItem?) {
        val context = binding.root.context
        item?.let { i ->
            binding.titleTxt.text = i.title
            binding.dateTxt.text = context.getString(R.string.item_content, i.site.name, i.getRegexDate(context))
            var liked = false
            if (RssItemManager.likedNewsList.find { it == i } != null)
                liked = true
            if (liked) binding.likeBtn.setColorFilter(R.color.black) else binding.likeBtn.clearColorFilter()
            binding.likeBtn.setOnClickListener {
                if (liked) {
                    Snackbar.make(
                        binding.root,
                        context.getString(R.string.str_already_liked),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                RssItemManager.addLikedItem(i)
                binding.likeBtn.setColorFilter(R.color.black)
            }
            binding.shareBtn.setOnClickListener { v ->
                binding.shareBtn.setColorFilter(R.color.black)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.link)
                shareIntent.type = "text/plain"
                context.startActivity(shareIntent)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(200)
                    binding.shareBtn.clearColorFilter()
                }
            }

                binding.root.setOnClickListener {
                    val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(i.link))
                    context.startActivity(actionIntent)
                }
            }
        }
    }

