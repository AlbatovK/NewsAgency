package com.albatros.newsagency

import android.content.Context
import androidx.annotation.PluralsRes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class RssItem(
    val site: Site,
    val link: String,
    val title: String,
    categoryData: String,
    pubDate: String
) {

    private var categoryWords = HashSet<String>()
    var date: Date = Date(System.currentTimeMillis())
    var liked = false

    init {
        for (pattern in ApplicationContext.datePatterns)
            try { date = SimpleDateFormat(pattern, Locale.US).parse(pubDate) ?: Date(1000) } catch (ignored: Exception) { }
        categoryData.plus(" $title ").trim().lowercase().split(' ').forEach { it -> categoryWords.add(it.filter { it.isLetterOrDigit() } ) }
        link.lowercase().split('\\').forEach { categoryWords.add(it) }
    }

    override fun equals(other: Any?): Boolean =
        if (other is RssItem) other.title.trim().equals(title.trim(), ignoreCase = true) else false

    fun getRegexDate(context: Context): String {
        val nowDate = Calendar.getInstance().timeInMillis
        var time = (nowDate - this.date.time).toDouble() / (1000 * 60 * 60 * 24)

        fun getQString(@PluralsRes id: Int, quantity: Int) = context.resources.getQuantityString(id, quantity)

        var date = time.toInt().toString() + " " + getQString(R.plurals.day_plurals, time.toInt())
        if (time < 1) {
            time = (nowDate - this.date.time).toDouble() / (1000 * 60 * 60)
            date = time.toInt().toString() + " " + getQString(R.plurals.hour_plurals, time.toInt())
            if (time < 1) {
                time = (nowDate - this.date.time).toDouble() / (1000 * 60)
                date = (time.toInt()).toString() + " " + getQString(R.plurals.min_plurals, time.toInt())
                if (time < 1) {
                    time = (nowDate - this.date.time).toDouble() / 1000
                    date = time.toInt().toString() + " " + getQString(R.plurals.sec_plurals, time.toInt())
                }
            }
        }
        return date
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + categoryWords.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }
}