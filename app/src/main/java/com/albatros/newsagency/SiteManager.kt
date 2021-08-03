package com.albatros.newsagency

import android.content.Context
import com.albatros.newsagency.database.SiteDao

object SiteManager {

    var siteList: MutableList<Site> = mutableListOf()
    lateinit var dao: SiteDao

    val sitesCount: Int
        get() = siteList.size

    suspend fun addSite(vararg sites: Site) {
        for (site in sites) {
            siteList.add(site)
            dao.insertSite(site)
        }
    }

    suspend fun deleteSite(vararg sites: Site) {
        for (site in sites) {
            siteList.remove(site)
            dao.deleteSite(site)
        }
    }

    suspend fun init() { siteList = dao.getSites().toMutableList() }

    fun clear() = siteList.clear()

    fun getSiteByName(name: String): Site =
        siteList.find { name == it.name } ?: Site(name, "?")
}