package com.albatros.newsagency

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.databinding.ActivityNavBinding
import com.albatros.newsagency.databinding.DialogBarcodeBinding
import com.google.android.material.snackbar.Snackbar


class NavActivity : AppCompatActivity() {

    lateinit var binding: ActivityNavBinding

    private fun setState() {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onBackPressed() {
        val favDoc = XmlFeedParser.createDocOf(RssItemManager.likedNewsList)
        FileManager.intoFile(favDoc, FileManager.liked_news_storage, binding.root.context)
        val delDoc = XmlFeedParser.createDocOf(RssItemManager.deletedList)
        FileManager.intoFile(delDoc, FileManager.deleted_news_storage, binding.root.context)
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)
        setState()

        val navController = findNavController(R.id.nav_host_fragment_activity_nav)
        binding.navView.setupWithNavController(navController)
        binding.navView.getOrCreateBadge(R.id.navigation_dashboard).number = RssItemManager.likedNewsList.size
        binding.navView.getOrCreateBadge(R.id.navigation_home).number = RssItemManager.itemsCount

        val message = getString(
            R.string.upload_news_data, RssItemManager.itemsCount,
            applicationContext.resources.getQuantityString(
                R.plurals.items_plurals,
                RssItemManager.itemsCount
            ),
            SiteManager.sitesCount,
            resources.getQuantityString(R.plurals.sites_plurals, SiteManager.sitesCount)
        )
        val msg: Snackbar = if (RssItemManager.isEmpty)
            Snackbar.make(binding.root, getString(R.string.net_exception), Snackbar.LENGTH_LONG)
        else Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

        val params = msg.view.layoutParams as CoordinatorLayout.LayoutParams
        params.anchorId = binding.navView.id
        params.anchorGravity = Gravity.TOP
        params.gravity = Gravity.TOP
        msg.view.layoutParams = params
        msg.show()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        setState()
    }

    override fun onResume() {
        super.onResume()
        setState()

    }

    override fun onStop() {
        super.onStop()
        val favDoc = XmlFeedParser.createDocOf(RssItemManager.likedNewsList)
        FileManager.intoFile(favDoc, FileManager.liked_news_storage, binding.root.context)
        val delDoc = XmlFeedParser.createDocOf(RssItemManager.deletedList)
        FileManager.intoFile(delDoc, FileManager.deleted_news_storage, binding.root.context)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_nav, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.exit -> {
                finishAffinity()
                true
            }
            R.id.scan_qr -> {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.create_qr -> {
                val builder = AlertDialog.Builder(this)
                val dialogBinding = DialogBarcodeBinding.inflate(layoutInflater)
                val data = BarcodeProcessor.getCiphered(SiteManager.siteList)
                val bitmap = BarcodeProcessor.createBarcodeFrom(data)
                dialogBinding.barcodeImg.setImageBitmap(bitmap)
                builder
                    .setView(dialogBinding.root)
                    .setPositiveButton(R.string.back_msg) { _, _ -> closeOptionsMenu() }
                    .setTitle(R.string.qr_info)
                    .create()
                    .show()
                true
            }
            else -> true
        }
    }
}