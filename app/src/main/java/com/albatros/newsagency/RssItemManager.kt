package com.albatros.newsagency

object RssItemManager {
    var newsList: MutableList<RssItem> = mutableListOf()
    var likedNewsList: MutableList<RssItem> = mutableListOf()

    fun addItem(vararg items: RssItem) {
        for (item in items)
            newsList.add(item)
    }

    fun addLikedItem(vararg items: RssItem) {
        for (item in items)
            likedNewsList.add(item)
    }

    fun clearNews() = newsList.clear()

    fun clearLikedNews() = likedNewsList.clear()
}