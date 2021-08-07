package com.albatros.newsagency.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.ui.NavActivity
import com.albatros.newsagency.R
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.adapters.rss.RssAdapter
import com.albatros.newsagency.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val touchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            rcv: RecyclerView,
            vh: RecyclerView.ViewHolder,
            trg: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
            if (dir == ItemTouchHelper.LEFT) {
                RssItemManager.removeLikedItemAt(vh.adapterPosition)
                (binding.likedList.adapter as RssAdapter).notifyItemRemoved(vh.adapterPosition)
                NavActivity.increaseBottomBadge(R.id.navigation_dashboard, -1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        retainInstance = true
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.likedList.adapter = RssAdapter(RssItemManager.likedNewsList)
        binding.likedList.layoutManager = LinearLayoutManager(container?.context)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.likedList)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.likedList.adapter?.notifyDataSetChanged()
    }
}