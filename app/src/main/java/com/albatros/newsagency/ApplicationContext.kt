package com.albatros.newsagency

import android.app.Application

import androidx.room.Room
import com.albatros.newsagency.database.SiteDatabase

class ApplicationContext : Application() {

    companion object {
        lateinit var instance: ApplicationContext
        const val dbName = "database"
        val datePatterns = listOf("E, d MMM YYY H:m:s z", "E MMM dd HH::mm::ss z yyyy")
    }

    lateinit var db: SiteDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        db = Room.databaseBuilder(applicationContext, SiteDatabase::class.java, dbName).build()
    }
}