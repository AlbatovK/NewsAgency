package com.albatros.newsagency.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.albatros.newsagency.*
import com.albatros.newsagency.adapters.RssAdapter
import com.albatros.newsagency.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    companion object {
        private var pos: Int = 0
    }

    private val refresher = SwipeRefreshLayout.OnRefreshListener {
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.menu?.forEach {
            it.isEnabled = false
        }
        RssItemManager.clearNews()
        if (this@HomeFragment.isVisible) {
            (binding.rssList.adapter as RssAdapter).notifyDataSetChanged()
            binding.downloadingHint.visibility = View.VISIBLE
        }
        val updating = lifecycleScope.launch(Dispatchers.Main) {
            val delta = 500.toLong()
            if (this@HomeFragment.isVisible) {
                while (true) {
                    binding.downloadingHint.text = getString(R.string.downloading_hint_1)
                    delay(delta)
                    binding.downloadingHint.text = getString(R.string.downloading_hint_2)
                    delay(delta)
                    binding.downloadingHint.text = getString(R.string.downloading_hint_3)
                    delay(delta)
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            SiteManager.clear()
            SiteManager.init()
            for (site in SiteManager.siteList) {
                try { NetLoader.loadFromSite(site) } catch (e: Exception) { }
                delay(100)
            }
            lifecycleScope.launch(Dispatchers.Main) {
                updating.cancel()
                activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.menu?.forEach {
                    it.isEnabled = true
                }
                (activity as NavActivity).binding.navView.getOrCreateBadge(R.id.navigation_home).number = RssItemManager.itemsCount
                if (this@HomeFragment.isVisible) {
                    binding.swipeContainer.isRefreshing = false
                    binding.downloadingHint.visibility = View.INVISIBLE
                    (binding.rssList.adapter as RssAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    private val touchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            rcv: RecyclerView,
            vh: RecyclerView.ViewHolder,
            trg: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
            if (dir == ItemTouchHelper.LEFT) {
                RssItemManager.removeItemAt(vh.adapterPosition)
                (binding.rssList.adapter as RssAdapter).notifyItemRemoved(vh.adapterPosition)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        retainInstance = true
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.rssList)
        binding.rssList.layoutManager = LinearLayoutManager(container?.context)
        binding.rssList.adapter = RssAdapter(RssItemManager.newsList)
        binding.swipeContainer.setOnRefreshListener(refresher)
        binding.rssList.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.rssList.scrollToPosition(pos)
                binding.rssList.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        pos = (binding.rssList.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        Toast.makeText(binding.root.context, pos.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden)
            pos = (binding.rssList.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        else
            binding.rssList.scrollToPosition(pos)
    }


    override fun onResume() {
        super.onResume()
        (binding.rssList.adapter as RssAdapter).notifyItemRangeChanged(0, (binding.rssList.adapter as RssAdapter).itemCount)
        binding.downloadingHint.visibility = View.INVISIBLE
        binding.swipeContainer.isRefreshing = false
        binding.rssList.smoothScrollToPosition(pos)
    }
}