package com.albatros.newsagency

import android.app.Application
import android.util.Xml
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.albatros.newsagency.database.SiteDatabase
import com.albatros.newsagency.utils.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class ApplicationContext : Application() {

    companion object {
        lateinit var instance: ApplicationContext
        const val dbName = "database"

        val datePatterns = arrayOf(
            "E, d MMM yyyy H:m:s z",
            "E MMM dd HH:mm:ss z yyyy"
        )

        val defaultSites = arrayOf(
            Site("Habr", "https://habr.com/ru/rss/all/all"),
            Site("HiNews", "https://hi-news.ru/feed"),
            Site("Ixbt News", "https://www.ixbt.com/export/news.rss"),
            Site("Новости IT", "https://novostit.com/feed"),
            Site("BBC Tech", "http://feeds.bbci.co.uk/news/technology/rss.xml"),
            Site("BBC News", "http://feeds.bbci.co.uk/news/world/rss.xml"),
            Site("Лента.Ру", "https://lenta.ru/rss/top7"),
            Site("Habrahabr", "https://habrahabr.ru/rss/hubs")
        )
    }

    private lateinit var db: SiteDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        db = Room.databaseBuilder(applicationContext, SiteDatabase::class.java, dbName).build()
        SiteManager.dao = db.getSiteDao()
        val init = GlobalScope.launch(Dispatchers.IO) {
            SiteManager.init()
        }
        val manager = PreferenceManager(this)
        val createdTag = manager.getString(PreferenceManager.CREATED_KEY, PreferenceManager.NONE_CREATED, this)
        if (createdTag == PreferenceManager.NONE_CREATED) {
            manager.setValueByKey(PreferenceManager.PreferencePair(PreferenceManager.CREATED_KEY, PreferenceManager.CREATED))
            GlobalScope.launch(Dispatchers.IO) {
                init.join()
                for (site in defaultSites)
                    SiteManager.addSite(site)
            }
        }
        val liked = FileManager.readFile(this, FileManager.liked_news_storage)
        val likedList = XmlFeedParser.parseFeedFrom(liked, null, external = false)
        RssItemManager.likedNewsList = likedList.toMutableList()
    }
}