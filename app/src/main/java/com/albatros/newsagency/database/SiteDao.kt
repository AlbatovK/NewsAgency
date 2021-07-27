package com.albatros.newsagency.database

import androidx.room.OnConflictStrategy
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.room.Query

import com.albatros.newsagency.Site

@Dao
interface SiteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSite(site: Site)

    @Delete
    suspend fun deleteSite(site: Site)

    @Update
    suspend fun updateSite(site: Site)

    @Query(value = "Select * From site Order By name Desc")
    suspend fun getSites(): List<Site>
}