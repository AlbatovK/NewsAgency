package com.albatros.newsagency

import android.content.Context
import android.content.res.AssetManager
import org.w3c.dom.Document
import java.io.InputStream
import java.util.*
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object FileManager {

    /**
     * Get raw data from any file in a system
     * Provides no parsing whatsoever
     */
    fun readFile(context: Context, name: String): String {
        context.openFileOutput(name, Context.MODE_APPEND)
        val stream: InputStream = context.openFileInput(name)
        val gotXml = StringBuilder()
        val scanner = Scanner(stream)
        while (scanner.hasNext())
            gotXml.append(scanner.nextLine())
        return gotXml.toString().also {
            scanner.close()
        }
    }

    /**
     * Deletes a file if the file exist and is supported by the app
     */
    fun deleteFile(context: Context, name: String): Boolean {
        context.openFileOutput(name, Context.MODE_APPEND)
        if (name in arrayOf(liked_news_storage, deleted_news_storage, tags_storage))
            return context.deleteFile(name)
        return false
    }

    /**
     * Gets database pre-set data from sites-table.sql
     */
    fun getAssetData(manager: AssetManager): ArrayList<String> {
        val exeRows = ArrayList<String>()
        val inputStream = manager.open(asset_name)
        val scanner = Scanner(inputStream)
        while (scanner.hasNext())
            exeRows.add(scanner.nextLine())
        return exeRows.also {
            inputStream.close()
            scanner.close()
        }
    }

    /**
     * Writes xml document into file
     * Returns result of action
     */
    fun intoFile(doc: Document, fileName: String, context: Context): Boolean {
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(doc)
        if (deleteFile(context, fileName) && fileName in arrayOf(liked_news_storage, deleted_news_storage, tags_storage)) {
            val result = StreamResult(context.openFileOutput(fileName, Context.MODE_APPEND))
            transformer.transform(source, result)
            return true
        }
        return  false
    }

    var asset_name           = "sites_table.sql"
    var liked_news_storage   = "news.xml"
    var deleted_news_storage = "read.xml"
    var tags_storage         = "tags.xml"
}