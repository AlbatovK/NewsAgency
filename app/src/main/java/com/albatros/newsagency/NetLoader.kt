package com.albatros.newsagency

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

object NetLoader {

    private fun getSiteContent(site: Site): Document {
        val xml = Jsoup.connect(site.url).get().toString()
        return Jsoup.parse(xml, "", Parser.xmlParser())
    }

    fun loadFromSite(from: Site) {
        val doc = getSiteContent(from)
        XmlFeedParser.parseFeedFromXml(doc.toString(), from).forEach {
            RssItemManager.addItem(it)
        }
    }
}