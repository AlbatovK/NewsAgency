package com.albatros.newsagency.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.albatros.newsagency.Site

@Database(entities = [Site::class], version = 1, exportSchema = false)
abstract class SiteDatabase : RoomDatabase() {
    abstract fun getSiteDao(): SiteDao
}