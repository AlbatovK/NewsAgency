package com.albatros.newsagency.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.albatros.newsagency.Site
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object BarcodeProcessor {

    /**
     * Create barcode of given parameters of Qrcode type
     */
    fun createBarcodeFrom(res: String, width: Int = 500, height: Int = 500): Bitmap? {
        val matrix = try { MultiFormatWriter().encode(res, BarcodeFormat.QR_CODE, width, height, null) }
        catch (exception: IllegalArgumentException) { return null }
        val pixels = IntArray(width * height)
        for (y in 0 until height)
            for (x in 0 until width)
                pixels[y * width + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            this.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    /**
     * Gets string to process into barcode
     */
    fun getCiphered(sites: List<Site>): String = XmlFeedParser.createStringOf(sites)

}