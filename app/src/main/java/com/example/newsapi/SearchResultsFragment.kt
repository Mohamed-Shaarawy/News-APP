package com.example.newsapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    private var NewsImage = ArrayList<String?>()
    private var NewsTitle = ArrayList<String>()
    private var NewsDesc = ArrayList<String>()
    private var NewsDate = ArrayList<String>()
    private var NewsSource = ArrayList<String>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)

    {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        btnReturn = view.findViewById(R.id.buttonReturn)

        searchQuery = arguments?.let {SearchResultsFragmentArgs.fromBundle(it).searchQuery } ?: return

        retrofit.searchArticles(searchQuery, API_Key).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(
                    call: Call<NewsResponse>,
                    response: Response<NewsResponse>
                ) {
                    NewsTitle.clear()
                    NewsDesc.clear()
                    NewsImage.clear()
                    NewsDate.clear()
                    NewsSource.clear()

                    if (response.isSuccessful) {
                        val articles = response.body()?.articles ?: emptyList()

                        for (article in articles) {
                            NewsTitle.add(article.title)
                            NewsDesc.add(article.description ?: "No description")
                            NewsImage.add(article.urlToImage)
                            NewsDate.add(article.publishedAt)
                            NewsSource.add(article.source.name)
                        }
                    } else {
                        NewsImage.add(R.drawable.imageloadfailed.toString())
                        NewsTitle.add("API Error")
                        NewsDesc.add("Failed to fetch news. Please try again later.")
                        NewsDate.add("Unknown Date")
                        NewsSource.add("NewsAPI")

                        Log.e("NewsRecyclerFragment", "Showing fallback data because API failed")
                        Log.e("NewsRecyclerFragment", "API response code: ${response.code()}")
                        Log.e("NewsRecyclerFragment", "Error body: ${response.errorBody()?.string()}")
                        Log.e("NewsRecyclerFragment", "Search Query: $searchQuery")
                    }

                    newsAdapter = NewsAdapter(
                        NewsImage,
                        NewsTitle,
                        NewsDesc,
                        NewsDate,
                        NewsSource,
                        requireContext()
                    )
                    recyclerView.adapter = newsAdapter
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    // Fallback dummy data in case API fails
                    NewsImage.add(R.drawable.imageloadfailed.toString())
                    NewsTitle.add("Connection Error")
                    NewsDesc.add("Unable to load news. Check your internet connection.")
                    NewsDate.add("Unknown Date")
                    NewsSource.add("Offline")

                    newsAdapter = NewsAdapter(
                        NewsImage,
                        NewsTitle,
                        NewsDesc,
                        NewsDate,
                        NewsSource,
                        requireContext()
                    )
                    recyclerView.adapter = newsAdapter

                    Log.e("NewsRecyclerFragment", "API Error: ${t.message}")
                }
            })

        btnReturn.setOnClickListener{
            findNavController().navigate(R.id.action_searchResultsFragment_to_newsRecyclerFragment)
        }
    }
}
