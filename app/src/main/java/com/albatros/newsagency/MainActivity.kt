package com.albatros.newsagency

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.adapters.RssAdapter
import com.albatros.newsagency.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.rssList.layoutManager = LinearLayoutManager(this)
        binding.rssList.adapter = RssAdapter(RssItemManager.newsList)

        val touchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = true

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        RssItemManager.newsList.removeAt(viewHolder.adapterPosition)
                        (binding.rssList.adapter as RssAdapter).notifyItemRemoved(viewHolder.adapterPosition)
                    }
                }
            })
        touchHelper.attachToRecyclerView(binding.rssList)
        binding.fab.setOnClickListener {
            binding.rssList.smoothScrollToPosition(0)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_nav, menu)
        return true
    }

}