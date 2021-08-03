package com.albatros.newsagency

object RssItemManager {

    var newsList: MutableList<RssItem> = mutableListOf()
    var likedNewsList: MutableList<RssItem> = mutableListOf()

    val isEmpty: Boolean
        get() = newsList.isEmpty()
    val itemsCount: Int
        get() = newsList.size
    val likedItemsCount: Int
        get() = likedNewsList.size

    fun addItem(vararg items: RssItem) { for (item in items) newsList.add(item) }
    fun addLikedItem(vararg items: RssItem) { for (item in items) likedNewsList.add(item) }

    fun removeItem(vararg items: RssItem) { for (item in items) newsList.remove(item) }
    fun removeLikedItem(vararg items: RssItem) { for (item in items) likedNewsList.remove(item) }
    fun removeItemAt(index: Int) = newsList.removeAt(index)

    fun clearNews() = newsList.clear()
    fun clearLikedNews() = likedNewsList.clear()
}