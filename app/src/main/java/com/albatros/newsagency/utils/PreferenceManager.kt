package com.albatros.newsagency.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val editor: SharedPreferences.Editor

    init {
        val settings = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_MULTI_PROCESS)
        editor = settings.edit()
        editor.apply()
        editor.commit()
    }

    class PreferencePair(val key: String, val value: String)

    fun setValueByKey(vararg pairs: PreferencePair) {
        for (pair in pairs)
            editor.putString(pair.key, pair.value)
        editor.apply()
        editor.commit()
    }

    fun getString(key: String, def: String, context: Context): String? {
        val settings = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_MULTI_PROCESS)
        return settings.getString(key, def)
    }

    companion object {
        const val SETTINGS_NAME = "settings"
        const val SORT_KEY = "sort_key"
        const val SORT_BY_DATE = "sort_by_date"
        const val SORT_BY_SITE = "sort_by_site"
        const val SORT_BY_SIZE = "sort_by_size"
        const val FILTER_KEY       = "mode_key"
        const val FILTER_MODE      = "filter_mode"
        const val NONE_FILTER_MODE = "none_filter_mode"
        const val CREATED_KEY = "created_key"
        const val CREATED = "created"
        const val NONE_CREATED = "none_created"
    }
}
