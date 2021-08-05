package com.albatros.newsagency.containers

import com.albatros.newsagency.utils.ItemComparators
import com.albatros.newsagency.RssItem
import java.util.*

object RssItemManager {

    var newsList: MutableList<RssItem> = mutableListOf()
    var likedNewsList: MutableList<RssItem> = mutableListOf()
    var deletedList: MutableList<RssItem> = mutableListOf()

    val isEmpty: Boolean
        get() = newsList.isEmpty()
    val itemsCount: Int
        get() = newsList.size
    val likedItemsCount: Int
        get() = likedNewsList.size

    fun getComparator(type: ItemComparators): Comparator<RssItem> {
        return when (type) {
            ItemComparators.SORT_BY_SIZE ->
                Comparator<RssItem> { n_1: RssItem, n_2: RssItem -> n_2.title.length.compareTo(n_1.title.length) }
            ItemComparators.SORT_BY_SITE ->
                Comparator<RssItem> { n_1: RssItem, n_2: RssItem -> n_1.site.name.compareTo(n_2.site.name, ignoreCase = true) }
            ItemComparators.SORT_BY_DATE ->
                Comparator<RssItem> { n_1: RssItem, n_2: RssItem -> n_2.date.compareTo(n_1.date) }
        }
    }

    fun addItem(vararg items: RssItem) { for (item in items) newsList.add(item) }
    fun addLikedItem(vararg items: RssItem) { for (item in items) likedNewsList.add(item) }
    fun deleteItem(vararg items: RssItem) { for (item in items) deletedList.add(item) }

    fun removeItem(vararg items: RssItem) { for (item in items) newsList.remove(item) }
    fun removeLikedItem(vararg items: RssItem) { for (item in items) likedNewsList.remove(item) }
    fun removeItemAt(index: Int) = newsList.removeAt(index)
    fun removeLikedItemAt(index: Int) = likedNewsList.removeAt(index)

    fun clearNews() = newsList.clear()
    fun clearLikedNews() = likedNewsList.clear()
}