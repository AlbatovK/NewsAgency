package com.albatros.newsagency

import com.albatros.newsagency.containers.SiteManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XmlFeedParser {

    private fun parseItemFromXml(element: Element, from: Site): RssItem {
        return RssItem(from,
            element.select(link_tag).text(),
            element.select(title_tag).text(),
            element.select(category_tag).text() + " " +
                    element.select(description_tag).text() + " " +
                    from.name.lowercase(),
            element.select(date_tag).text()
        )
    }

    /**
     * External = true for parsing from distant net source, false for parsing from internal storage sources
     */
    fun parseFeedFrom(feed: String, from: Site?, external: Boolean = true): List<RssItem> {
        val parser: Parser = Parser.xmlParser()
        val doc: Document = Jsoup.parse(feed, "", parser)
        if (external) from?.imageLink = doc.select(image_tag).text()
        return doc.select(item_tag).map { parseItemFromXml(it,
            if (external) from!! else SiteManager.getSiteByName(it.select(site_tag).text()))
        }
    }

    /**
     * Xml representation of sites contained on device
     * Used for creating info barcode
     */
    fun createStringOf(sites: List<Site>): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val root = doc.createElement(sites_root_tag)
        for (site in sites) {
            val siteElem = doc.createElement(site_tag)
            val nameTag = doc.createElement(name_tag)
            nameTag.appendChild(doc.createTextNode(site.name))
            siteElem.appendChild(nameTag)
            val urlTag = doc.createElement(url_tag)
            urlTag.appendChild(doc.createTextNode(site.url))
            siteElem.appendChild(urlTag)
            root.appendChild(siteElem)
        }
        doc.appendChild(root)
        val transformer = TransformerFactory.newInstance().newTransformer()
        val writer = StringWriter()
        val result = StreamResult(writer)
        val source = DOMSource(doc)
        transformer.transform(source, result)
        return writer.toString()
    }

    /**
     * Creates xml representation of RssItems in order to be written into storage for later use
     * See FileManager and storage tags
     */
    fun createDocOf(items: List<RssItem>): org.w3c.dom.Document {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val rootElem = doc.createElement(rss_root_tag)
        for (item in items) {
            val itemElem = doc.createElement(item_tag)
            val nameElem = doc.createElement(title_tag)
            nameElem.appendChild(doc.createTextNode(item.title))
            val siteElem = doc.createElement(site_tag)
            siteElem.appendChild(doc.createTextNode(item.site.name))
            val urlElem = doc.createElement(link_tag)
            urlElem.appendChild(doc.createTextNode(item.link))
            val dateElem = doc.createElement(date_tag)
            dateElem.appendChild(doc.createTextNode(item.date.toString()))
            itemElem.appendChild(nameElem)
            itemElem.appendChild(siteElem)
            itemElem.appendChild(urlElem)
            itemElem.appendChild(dateElem)
            rootElem.appendChild(itemElem)
        }
        doc.appendChild(rootElem)
        return doc
    }

    private var rss_root_tag    = "items"
    private var item_tag        = "item"
    private var link_tag        = "link"
    private var title_tag       = "title"
    private var category_tag    = "category"
    private var description_tag = "description"
    private var date_tag        = "pubDate"
    private var image_tag       = "url"
    private var sites_root_tag  = "sites"
    private var site_tag        = "site"
    private var name_tag        = "name"
    private var url_tag         = "url"
}