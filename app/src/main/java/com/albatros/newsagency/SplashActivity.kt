package com.albatros.newsagency

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.albatros.newsagency.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val appContext = ApplicationContext.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splash_custom_theme)
        supportActionBar?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenStarted {
            val launcher = lifecycleScope.launch(Dispatchers.IO) {
                appContext.db.getSiteDao().insertSite(Site("Site", "https://habr.com/rss/all"))
                val siteList = appContext.db.getSiteDao().getSites()
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.progressBar.max = siteList.size
                }
                for (site in siteList) {
                    try { NetLoader.loadFromSite(site) } catch (e: Exception) { }
                    val update = lifecycleScope.launch(Dispatchers.Main) {
                        binding.progressBar.incrementProgressBy(1)
                    }
                    delay(100)
                    update.join()
                }
            }
            launcher.join()
            lifecycleScope.launch(Dispatchers.Main) {
                binding.motionLayout.transitionToEnd()
                delay(1500)
                //val intent = Intent(applicationContext, MainNavigationActivity::class.java)
                //startActivity(intent)
                //overridePendingTransition(0, R.xml.alpha_transition)
            }
        }
    }
}