package com.albatros.newsagency

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.HashSet

class RssItem(
    val site: Site,
    val link: String,
    val title: String,
    categoryData: String,
    pubDate: String
) {

    var categoryWords = HashSet<String>()
    lateinit var date: Date

    init {
        for (pattern in ApplicationContext.datePatterns)
            try { date = SimpleDateFormat(pattern, Locale.US).parse(pubDate) ?: Date(1000) } catch (ignored: Exception) { }
        categoryData.plus(" $title ").trim().lowercase().split(' ').forEach { it -> categoryWords.add(it.filter { it.isLetterOrDigit() } ) }
        link.lowercase().split('\\').forEach { categoryWords.add(it) }
    }

    override fun equals(other: Any?): Boolean =
        if (other is RssItem) other.title.equals(title, ignoreCase = true) else false
}