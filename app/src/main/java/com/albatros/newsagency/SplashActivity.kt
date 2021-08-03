package com.albatros.newsagency

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import androidx.work.ExistingPeriodicWorkPolicy
import com.albatros.newsagency.databinding.ActivitySplashBinding
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

    private fun setWindowState(hideToolBar: Boolean = true) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        if (hideToolBar) supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
        setWindowState()
        setContentView(binding.root)
        binding.progressBar.max = SiteManager.siteList.size * maxBarCount
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