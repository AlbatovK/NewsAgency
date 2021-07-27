package com.albatros.newsagency

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

object XmlFeedParser {

    private fun parseItemFromXml(element: Element, from: Site): RssItem {
        return RssItem(
            from,
            element.select(link_tag).text(),
            element.select(title_tag).text(),
            element.select(category_tag).text() + " " +
                    element.select(description_tag).text() + " " +
                    from.name.lowercase(),
            element.select(date_tag).text(),
        )
    }

    fun parseFeedFromXml(feed: String, from: Site): List<RssItem> {
        val parser: Parser = Parser.xmlParser()
        val doc: Document = Jsoup.parse(feed, "", parser)
        return doc.select(item_tag).map { parseItemFromXml(it, from) }
    }

    var item_tag = "item"
    var link_tag = "link"
    var title_tag = "title"
    var category_tag = "category"
    var description_tag = "description"
    var date_tag = "pubDate"
}