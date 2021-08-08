package com.albatros.newsagency.ui.notifications

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.albatros.newsagency.R
import com.albatros.newsagency.adapters.site.SiteAdapter
import com.albatros.newsagency.containers.RssItemManager
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.databinding.FragmentNotificationsBinding
import com.albatros.newsagency.ui.NavActivity
import com.albatros.newsagency.utils.FileManager
import com.albatros.newsagency.utils.ItemComparators
import com.albatros.newsagency.utils.PreferenceManager
import com.albatros.newsagency.utils.XmlFeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding

    override fun onResume() {
        super.onResume()
        binding.siteList.adapter?.notifyDataSetChanged()
    }

    private fun setPreferenceState(
        settings: SharedPreferences,
        group: RadioGroup,
        mode: String, map: HashMap<Int, String>,
        std_mode: String
    ) {
        val keyIterator: Iterator<Int> = map.keys.iterator()
        val valueIterator: Iterator<String> = map.values.iterator()
        while (valueIterator.hasNext() && keyIterator.hasNext()) {
            if (settings.getString(mode, std_mode) == valueIterator.next()) {
                group.check(keyIterator.next())
                continue
            }
            keyIterator.next()
        }
    }

    private val touchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN) {
        override fun onMove(
            rcv: RecyclerView,
            vh: RecyclerView.ViewHolder,
            trg: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
            if (dir == ItemTouchHelper.DOWN) {
                NavActivity.increaseBottomBadge(R.id.navigation_notifications, -1)
                lifecycleScope.launch(Dispatchers.IO) {
                    SiteManager.deleteSiteAt(vh.adapterPosition)
                    launch(Dispatchers.Main) {
                        (binding.siteList.adapter as SiteAdapter).notifyItemRemoved(vh.adapterPosition)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        binding.editTxt.onRightDrawableClicked {
            it.text.clear()
            PreferenceManager(binding.root.context).setValueByKey(
                PreferenceManager.PreferencePair(
                    PreferenceManager.FILTER_KEY,
                    PreferenceManager.NONE_FILTER_MODE
                )
            )
            FileManager.deleteFile(binding.root.context, FileManager.tags_storage)

            val inputMethodManager =
                activity?.getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity?.currentFocus?.windowToken,
                    0
                )
                it.clearFocus()
            }
        }
        binding.editTxt.setOnEditorActionListener { v, id, event ->
            PreferenceManager(binding.root.context).setValueByKey(
                PreferenceManager.PreferencePair(
                    PreferenceManager.FILTER_KEY,
                    PreferenceManager.FILTER_MODE
                )
            )
            val inputMethodManager =
                activity?.getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity?.currentFocus?.windowToken,
                0
            )
            binding.editTxt.clearFocus()
            false
        }
        val lastData = XmlFeedParser.parseTagDoc(FileManager.readFile(binding.root.context, FileManager.tags_storage)).joinToString(", ")
        binding.editTxt.setText(lastData)

        binding.editTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.root.context.openFileOutput(FileManager.tags_storage, Context.MODE_APPEND)
                if (s?.length == 0)
                    return
                s?.let {
                    val tags = s.toString().replace(" ", "").split(",").toTypedArray()
                    val doc = XmlFeedParser.createDocOf(tags)
                    FileManager.intoFile(doc, FileManager.tags_storage, binding.root.context)
                }
                val inputMethodManager =
                    activity?.getSystemService(
                        Activity.INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                if (!inputMethodManager.isAcceptingText) {
                    inputMethodManager.hideSoftInputFromWindow(
                        activity?.currentFocus?.windowToken,
                        0
                    )
                    binding.editTxt.clearFocus()
                }
            }

        })



        binding.siteList.requestFocusFromTouch()
        binding.siteList.layoutManager =
            GridLayoutManager(binding.root.context, 2, GridLayoutManager.HORIZONTAL, false)
        binding.siteList.adapter = SiteAdapter(SiteManager.siteList)
        val itemTouchHelper = ItemTouchHelper(touchCallback)
        itemTouchHelper.attachToRecyclerView(binding.siteList)
        val settings: SharedPreferences =
            binding.root.context.getSharedPreferences(
                PreferenceManager.SETTINGS_NAME,
                Context.MODE_MULTI_PROCESS
            )
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
        NavActivity.bnd!!.appBar.setExpanded(true, true)
        return binding.root
    }


}