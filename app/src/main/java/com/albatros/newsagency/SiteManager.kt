package com.albatros.newsagency

import com.albatros.newsagency.database.SiteDao

object SiteManager {

    var siteList: MutableList<Site> = mutableListOf()
    lateinit var dao: SiteDao

    suspend fun addSite(vararg sites: Site) {
        for (site in sites) {
            siteList.add(site)
            dao.insertSite(site)
        }
    }

    suspend fun init() {
        siteList = dao.getSites().toMutableList()
    }
}