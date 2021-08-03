package com.albatros.newsagency.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.albatros.newsagency.*
import com.albatros.newsagency.adapters.RssAdapter
import com.albatros.newsagency.databinding.FragmentDashboardBinding
import java.io.StringWriter
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
retainInstance = true
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.likedList.adapter = RssAdapter(RssItemManager.likedNewsList)
        binding.likedList.layoutManager = LinearLayoutManager(container?.context)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.likedList.adapter?.notifyDataSetChanged()
        (activity as NavActivity).binding.navView.getOrCreateBadge(R.id.navigation_dashboard).number = RssItemManager.likedNewsList.size

    }

    override fun onStop() {
        val favDoc = XmlFeedParser.createDocOf(RssItemManager.likedNewsList)
        val transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(favDoc)
        val writer = StringWriter()
        val res = StreamResult(writer)
        transformer.transform(source, res)
        Log.d("XML", writer.toString())
        FileManager.intoFile(favDoc, FileManager.liked_news_storage, binding.root.context)
        super.onStop()
    }
}