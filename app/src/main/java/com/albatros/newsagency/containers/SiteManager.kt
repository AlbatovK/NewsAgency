package com.albatros.newsagency.containers

import com.albatros.newsagency.Site
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

    suspend fun deleteSiteAt(pos: Int) {
        if (pos < siteList.size) {
            val site = siteList[pos]
            site.imageLink = ""
            deleteSite(site)
        }
    }

    suspend fun init() { siteList = dao.getSites().toMutableList() }

    fun clear() = siteList.clear()

    fun getSiteByName(name: String): Site = siteList.find { name == it.name } ?: Site(name, "?")
}