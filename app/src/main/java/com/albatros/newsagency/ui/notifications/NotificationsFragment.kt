package com.albatros.newsagency.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.albatros.newsagency.ItemComparators
import com.albatros.newsagency.R
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.adapters.site.SiteAdapter
import com.albatros.newsagency.databinding.FragmentNotificationsBinding
import com.albatros.newsagency.ui.home.HomeFragment
import com.albatros.newsagency.utils.PreferenceManager
import java.util.*

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding

    private fun setPreferenceState(
        settings: SharedPreferences,
        group: RadioGroup,
        mode: String, map: HashMap<Int, String>,
        std_mode: String) {
        val keyIterator: Iterator<Int> = map.keys.iterator()
        val valueIterator: Iterator<String> = map.values.iterator()
        while (valueIterator.hasNext() && keyIterator.hasNext()) {
            var hadNext = false
            if (settings.getString(mode, std_mode) == valueIterator.next()) {
                group.check(keyIterator.next())
                hadNext = true
            }
            if (!hadNext) keyIterator.next()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        binding.siteList.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.siteList.adapter = SiteAdapter(SiteManager.siteList)
        val settings: SharedPreferences =
            binding.root.context.getSharedPreferences(PreferenceManager.SETTINGS_NAME, Context.MODE_MULTI_PROCESS)
        val editor = settings.edit()
        val sortGroup: RadioGroup = binding.sortList
        val sortMap = HashMap<Int, String>()
        sortMap[R.id.sort_by_date_button] = PreferenceManager.SORT_BY_DATE
        sortMap[R.id.sort_by_site_button] = PreferenceManager.SORT_BY_SITE
        sortMap[R.id.sort_by_size_button] = PreferenceManager.SORT_BY_SIZE
        setPreferenceState(
            settings,
            sortGroup,
            PreferenceManager.SORT_KEY,
            sortMap,
            PreferenceManager.SORT_BY_DATE
        )
        val modeGroup: RadioGroup = binding.modeList
        val modeMap = HashMap<Int, String>()
        modeMap[R.id.none_filter] = PreferenceManager.NONE_FILTER_MODE
        modeMap[R.id.filter_tag] = PreferenceManager.FILTER_MODE
        setPreferenceState(
            settings,
            modeGroup,
            PreferenceManager.FILTER_KEY,
            modeMap,
            PreferenceManager.NONE_FILTER_MODE
        )
        val sortChangeListener =
            RadioGroup.OnCheckedChangeListener { _, checkedId ->
                HomeFragment.pos = 0
                val manager = PreferenceManager(binding.root.context)
                when (checkedId) {
                    R.id.sort_by_date_button -> {
                        manager.setValueByKey(
                            PreferenceManager.PreferencePair(
                                PreferenceManager.SORT_KEY,
                                PreferenceManager.SORT_BY_DATE
                            )
                        )
                        Collections.sort(
                            RssItemManager.newsList,
                            RssItemManager.getComparator(ItemComparators.SORT_BY_DATE)
                        )
                    }
                    R.id.sort_by_site_button -> {
                        manager.setValueByKey(
                            PreferenceManager.PreferencePair(
                                PreferenceManager.SORT_KEY,
                                PreferenceManager.SORT_BY_SITE
                            )
                        )
                        Collections.sort(
                            RssItemManager.newsList,
                            RssItemManager.getComparator(ItemComparators.SORT_BY_SITE)
                        )
                    }
                    R.id.sort_by_size_button -> {
                        manager.setValueByKey(
                            PreferenceManager.PreferencePair(
                                PreferenceManager.SORT_KEY,
                                PreferenceManager.SORT_BY_SIZE
                            )
                        )
                        Collections.sort(
                            RssItemManager.newsList,
                            RssItemManager.getComparator(ItemComparators.SORT_BY_SIZE)
                        )
                    }
                }
                editor.apply()
            }
        val modeChangeListener =
            RadioGroup.OnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.none_filter)
                    editor.putString(
                        PreferenceManager.FILTER_KEY,
                        PreferenceManager.NONE_FILTER_MODE
                    )
                else if (checkedId == R.id.filter_tag)
                    editor.putString(PreferenceManager.FILTER_KEY, PreferenceManager.FILTER_MODE)
                editor.commit()
            }
        sortGroup.setOnCheckedChangeListener(sortChangeListener)
        modeGroup.setOnCheckedChangeListener(modeChangeListener)
        editor.commit()
        return binding.root
    }


}