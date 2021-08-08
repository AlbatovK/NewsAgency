package com.albatros.newsagency.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.albatros.newsagency.R
import com.albatros.newsagency.Site
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.databinding.ActivityNavBinding
import com.albatros.newsagency.databinding.AddSiteDialogBinding
import com.albatros.newsagency.databinding.DialogBarcodeBinding
import com.albatros.newsagency.utils.BarcodeProcessor
import com.albatros.newsagency.utils.FileManager
import com.albatros.newsagency.utils.XmlFeedParser
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


class NavActivity : AppCompatActivity() {

    lateinit var binding: ActivityNavBinding

    companion object {
        var bnd: ActivityNavBinding? = null
        lateinit var instance: NavActivity

        fun increaseBottomBadge(@IdRes id: Int, number: Int = 1, clear: Boolean = false) {
            bnd?.let {
                if (clear)
                    bnd!!.navView.getOrCreateBadge(id).number = 0
                bnd!!.navView.getOrCreateBadge(id).number = bnd!!.navView.getOrCreateBadge(id).number.plus(number)
            }
        }
    }

    private fun setState() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
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
        bnd = binding
        instance = this
        setContentView(binding.root)
        binding.toolbar.title = getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)
        setState()

        val navController = findNavController(R.id.nav_host_fragment_activity_nav)
        binding.navView.setupWithNavController(navController)
        increaseBottomBadge(R.id.navigation_notifications, SiteManager.sitesCount, clear = true)
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

    private fun checkSiteData(name: String, address: String) : Boolean {
        val urlPattern = Patterns.WEB_URL
        val matcher = urlPattern.matcher(address.lowercase())
        return if (name.isEmpty() || address.isEmpty() || !matcher.matches()) {
            Toast.makeText(applicationContext, getString(R.string.str_invalid_data), Toast.LENGTH_LONG).show()
            false
        } else {
            var valid = false
            Thread {
                try {
                    Jsoup.connect(address).get()
                    valid = true
                } catch (e: Exception) { }
            }.start()
            Thread.sleep(1000)
            valid
        }
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
                    .setPositiveButton(R.string.back_msg) { _, _ ->
                        closeOptionsMenu()
                        setState()
                    }
                    .setTitle(R.string.qr_info)
                    .create()
                    .show()
                true
            }
            R.id.add_site -> {
                val builder = AlertDialog.Builder(this)
                val viewBinding = AddSiteDialogBinding.inflate(layoutInflater)
                val alertDialog = builder
                    .setTitle(R.string.str_add_new_site)
                    .setMessage(R.string.str_add_site_descr_dialog)
                    .setIcon(R.drawable.rss_icon)
                    .setView(viewBinding.root)
                    .setNegativeButton(R.string.str_add_site_neg_button) { _, _ ->
                        setState()
                        closeOptionsMenu()
                    }
                    .setPositiveButton(R.string.str_add_site_pos_button) { _, _ ->
                        setState()
                        val name = viewBinding.editDialogNameTextView.text.toString().trim()
                        val address = viewBinding.editDialogAddressTextView.text.toString().trim()
                        if (checkSiteData(name, address))
                            lifecycleScope.launch(Dispatchers.IO) {
                                if (Site(name, address) !in SiteManager.siteList) {
                                    SiteManager.addSite(Site(name, address))
                                    launch(Dispatchers.Main) {
                                        increaseBottomBadge(R.id.navigation_notifications)
                                    }
                                }
                            }
                    }.create()
                alertDialog.show()
                true
            }
            else -> true
        }
    }
}