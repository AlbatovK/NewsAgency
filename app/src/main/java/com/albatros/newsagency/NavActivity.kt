package com.albatros.newsagency

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.albatros.newsagency.databinding.ActivityNavBinding
import com.albatros.newsagency.databinding.DialogBarcodeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class NavActivity : AppCompatActivity() {

    lateinit var binding: ActivityNavBinding

    fun dpToPx(dp: Int): Int =
        dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT).roundToInt()


    private fun setState() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_nav, menu)
        return true
    }

    private fun encodeAsBitmap(str: String): Bitmap? {
        val result: BitMatrix = try {
            MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 300, 300, null)
        } catch (iae: IllegalArgumentException) {
            return null
        }
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result[x, y]) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
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