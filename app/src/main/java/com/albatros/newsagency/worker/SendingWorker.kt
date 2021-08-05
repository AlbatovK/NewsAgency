package com.albatros.newsagency.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import androidx.work.ForegroundInfo
import androidx.work.ExistingPeriodicWorkPolicy
import com.albatros.newsagency.utils.NetLoader
import com.albatros.newsagency.R
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Random
import java.util.concurrent.TimeUnit

class SendingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        var work_id = "id_work"
        var interval_min: Long = 15
        var time_unit = TimeUnit.MINUTES
    }

    private var chnId = "id"
    private var chnName = "Main_Thread"
    private var channel: NotificationChannel? = null
    private var pos = 0
    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private fun createCurrentChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(chnId, chnName, NotificationManager.IMPORTANCE_HIGH)
            channel?.enableVibration(true)
        }
        try { channel?.let { notificationManager.createNotificationChannel(it) } }
        catch (ignored: Exception) { }
    }

    private fun getContentIntent(pos: Int): PendingIntent? {
        val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(RssItemManager.newsList[pos].link))
        return PendingIntent.getActivity(applicationContext, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNotification(contentIntent: PendingIntent?): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, chnId)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.news_icon)
                .setContentText(RssItemManager.newsList[pos].title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
        return builder.build()
    }

    private fun resetWork() {
        val request = PeriodicWorkRequest.Builder(SendingWorker::class.java, interval_min, time_unit)
            .setInitialDelay(interval_min, time_unit)
            .build()
        val manager = WorkManager.getInstance(applicationContext)
        manager.enqueueUniquePeriodicWork(work_id, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override suspend fun doWork(): Result = coroutineScope {
        RssItemManager.clearNews()
        val loader = launch(Dispatchers.IO) {
            val init = launch(Dispatchers.IO) {
                if (SiteManager.sitesCount == 0)
                    SiteManager.init()
            }
            init.join()
            val parsing = launch(Dispatchers.IO) {
                for (site in SiteManager.siteList)
                    try { NetLoader.loadFromSite(site) }
                    catch (e: Exception) { }
            }
            parsing.join()
        }
        loader.join()
        createCurrentChannel()
        pos = Random().nextInt(RssItemManager.itemsCount)
        val contentIntent = getContentIntent(pos)
        val notification = getNotification(contentIntent)
        val info = ForegroundInfo(0, notification, 0)
        setForegroundAsync(info)
        notificationManager.notify(12, notification)
        resetWork()
        RssItemManager.clearNews()
        Result.success()
    }
}