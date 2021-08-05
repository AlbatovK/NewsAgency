package com.albatros.newsagency.adapters.rss

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.R
import com.albatros.newsagency.RssItem
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.databinding.ItemLayoutBinding
import com.albatros.newsagency.ui.NavActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

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
            binding.dateTxt.text =
                context.getString(R.string.item_content, i.site.name, i.getRegexDate(context))
                if (i in RssItemManager.likedNewsList) binding.likeBtn.setColorFilter(R.color.black) else binding.likeBtn.clearColorFilter()
                binding.likeBtn.setOnClickListener {
                    if (i in RssItemManager.likedNewsList) {
                        val msg: Snackbar =
                            Snackbar.make(NavActivity.bnd.root, context.getString(R.string.str_already_liked), Snackbar.LENGTH_LONG)
                        val params = msg.view.layoutParams as CoordinatorLayout.LayoutParams
                        params.anchorId = R.id.nav_view
                        params.anchorGravity = Gravity.TOP
                        params.gravity = Gravity.TOP
                        msg.view.layoutParams = params
                        msg.view.setPadding(0, 0, 0, 0)
                        msg.show()
                    }
                    if (i !in RssItemManager.likedNewsList) {
                        val msg = Snackbar.make(
                            NavActivity.bnd.root, context.getString(R.string.str_liked_done),
                            Snackbar.LENGTH_SHORT
                        )
                        val params = msg.view.layoutParams as CoordinatorLayout.LayoutParams
                        params.anchorId = R.id.nav_view
                        params.anchorGravity = Gravity.TOP
                        params.gravity = Gravity.TOP
                        msg.view.layoutParams = params
                        msg.view.setPadding(0, 0, 0, 0)
                        msg.show()
                        RssItemManager.addLikedItem(i)
                        binding.likeBtn.setColorFilter(R.color.black)
                    }
                }

            binding.shareBtn.setOnClickListener {
                binding.shareBtn.setColorFilter(R.color.black)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.link)
                shareIntent.type = "text/plain"
                context.startActivity(shareIntent)
                MainScope().launch(Dispatchers.Main) {
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

