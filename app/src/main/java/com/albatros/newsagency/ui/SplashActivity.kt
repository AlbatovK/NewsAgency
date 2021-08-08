package com.albatros.newsagency.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.albatros.newsagency.R
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.databinding.ActivitySplashBinding
import com.albatros.newsagency.utils.NetLoader
import com.albatros.newsagency.worker.SendingWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val maxBarCount          = 1_000
    private val delay: Long          = 5
    private val inBetweenDelay: Long = 300
    private val onStopDelay: Long    = 1_500
    private val step                 = delay.toInt()

    private fun setState() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun launchWorker() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = PeriodicWorkRequest.Builder(SendingWorker::class.java, SendingWorker.interval_min, SendingWorker.time_unit)
            .setConstraints(constraints)
            .setInitialDelay(SendingWorker.interval_min, SendingWorker.time_unit)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SendingWorker.work_id,
            ExistingPeriodicWorkPolicy.REPLACE,
            request)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setState()
        actionBar?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        RssItemManager.clearNews()
        binding.progressBar.max = SiteManager.siteList.size * maxBarCount
        binding.progressBar.progress = 0
        lifecycleScope.launchWhenStarted {
            val launcher = lifecycleScope.launch(Dispatchers.IO) {
                for (site in SiteManager.siteList) {
                    try { NetLoader.loadFromSite(site) } catch (e: Exception) { }
                    val updateUI = lifecycleScope.launch(Dispatchers.Main) {
                        for (i in 1..maxBarCount / delay) {
                            delay(delay)
                            binding.progressBar.incrementProgressBy(step)
                        }
                    }
                    updateUI.join()
                    delay(inBetweenDelay)
                }
            }
            launcher.join()
            lifecycleScope.launch(Dispatchers.Main) {
                launchWorker()
                binding.motionLayout.transitionToEnd()
                delay(onStopDelay)
                val intent = Intent(applicationContext, NavActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, R.xml.alpha_transition)
            }
        }
    }
}