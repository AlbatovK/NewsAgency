package com.albatros.newsagency

import com.albatros.newsagency.containers.RssItemManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

object NetLoader {

    private fun getSiteContent(site: Site): Document {
        val xml = Jsoup.connect(site.url).get().toString()
        return Jsoup.parse(xml, "", Parser.xmlParser())
    }

    /**
     * Can throw net exceptions
     * Uses XmlFeedParser to parse site content (external = true)
     */
    fun loadFromSite(from: Site) {
        val doc = getSiteContent(from)
        XmlFeedParser.parseFeedFrom(doc.toString(), from).forEach { item ->
            if (RssItemManager.deletedList.find { it.title.equals(item.title, ignoreCase = true) } == null)
                RssItemManager.addItem(item)
        }
    }
}