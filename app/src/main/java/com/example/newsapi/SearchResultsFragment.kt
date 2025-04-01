package com.example.newsapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapi.api.NewsAPIService
import com.example.newsapi.model.NewsResponse
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SearchResultsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var searchQuery: String
    private lateinit var btnReturn: Button
    private lateinit var sortSpinner: Spinner
    private var selectedSortOption = "publishedAt" // Default sort option

    private var NewsImage = ArrayList<String?>()
    private var NewsTitle = ArrayList<String>()
    private var NewsDesc = ArrayList<String>()
    private var NewsDate = ArrayList<String>()
    private var NewsSource = ArrayList<String>()
    private var content = ArrayList<String?>()
    private var URL = ArrayList<String>()

    private val BASE_URL = "https://newsapi.org/"
    private val API_Key = "9a31420d0db94cbab8274e37c6fdcda5"

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder().header("User-Agent", "Mozilla/5.0 (Android 11; Mobile)")
            .method(original.method(), original.body()).build()
        chain.proceed(request)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client).build().create(NewsAPIService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        btnReturn = view.findViewById(R.id.buttonReturn)
        sortSpinner = view.findViewById(R.id.sortSpinner)

        searchQuery = arguments?.let { SearchResultsFragmentArgs.fromBundle(it).searchQuery } ?: return

        setupSortSpinner()
        fetchSortedArticles() // Initial load with default sort

        btnReturn.setOnClickListener {
            findNavController().navigate(R.id.action_searchResultsFragment_to_newsRecyclerFragment)
        }
    }

    private fun setupSortSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sortSpinner.adapter = adapter
        }

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSortOption = when (position) {
                    0 -> "relevancy"
                    1 -> "popularity"
                    2 -> "publishedAt"
                    else -> "publishedAt"
                }
                fetchSortedArticles()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if no selection
            }
        }
    }

    private fun fetchSortedArticles() {
        retrofit.sort(searchQuery, selectedSortOption, API_Key).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(
                call: Call<NewsResponse>,
                response: Response<NewsResponse>
            ) {
                NewsTitle.clear()
                NewsDesc.clear()
                NewsImage.clear()
                NewsDate.clear()
                NewsSource.clear()
                content.clear()
                URL.clear()

                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()

                    for (article in articles) {
                        NewsTitle.add(article.title)
                        NewsDesc.add(article.description ?: "No description")
                        NewsImage.add(article.urlToImage)
                        NewsDate.add(article.publishedAt)
                        NewsSource.add(article.source.name)
                        content.add(article.content)
                        URL.add(article.url)
                    }
                } else {
                    NewsImage.add(R.drawable.imageloadfailed.toString())
                    NewsTitle.add("API Error")
                    NewsDesc.add("Failed to fetch news. Please try again later.")
                    NewsDate.add("Unknown Date")
                    NewsSource.add("NewsAPI")

                    Log.e("SearchResultsFragment", "API response code: ${response.code()}")
                    Log.e("SearchResultsFragment", "Error body: ${response.errorBody()?.string()}")
                }

                updateRecyclerView()
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                NewsImage.add(R.drawable.imageloadfailed.toString())
                NewsTitle.add("Connection Error")
                NewsDesc.add("Unable to load news. Check your internet connection.")
                NewsDate.add("Unknown Date")
                NewsSource.add("Offline")

                Log.e("SearchResultsFragment", "API Error: ${t.message}")
                updateRecyclerView()
            }
        })
    }

    private fun updateRecyclerView() {
        newsAdapter = NewsAdapter(
            NewsImage,
            NewsTitle,
            NewsDesc,
            NewsDate,
            NewsSource,
            content,
            URL,
        ) { title, desc, imageUrl, date, source, content, url ->
            val action = SearchResultsFragmentDirections.actionSearchResultsFragmentToNewsDetailsFragment(
                title = title,
                desc = desc,
                imageUrl = imageUrl,
                date = date,
                source = source,
                content = content,
                url = url
            )
            findNavController().navigate(action)
        }
        recyclerView.adapter = newsAdapter
    }
}
