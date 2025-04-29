package com.example.newsapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.api.NewsAPIService
import com.example.newsapp.model.NewsResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import okhttp3.OkHttpClient

class NewsRecyclerFragment : Fragment() {

    val BASE_URL: String = "https://newsapi.org/"
    val API_Key: String = "9a31420d0db94cbab8274e37c6fdcda5"

    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Android 11; Mobile; rv:89.0) Gecko/89.0 Firefox/89.0"
                )
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }
        .build()

    val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(NewsAPIService::class.java)

    private val countryCodeMap = mapOf(
        "United States" to "us",
        "Italy" to "it",
        "Spain" to "es",
        "Germany" to "de",
        "France" to "fr",
    )
    val CHANNEL_ID = "1"

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

    private var NewsImage = ArrayList<String?>()
    private var NewsTitle = ArrayList<String>()
    private var NewsDesc = ArrayList<String>()
    private var NewsDate = ArrayList<String>()
    private var NewsSource = ArrayList<String>()
    private var content = ArrayList<String?>()
    private var URL = ArrayList<String>()
    lateinit var searchEditText: EditText
    lateinit var btnSearch: Button
    lateinit var bottomNav: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get the passed args
        val countryName = arguments?.getString("countryName") ?: "US"
        val categoryName = arguments?.getString("categoryName") ?: ""
        recyclerView = view.findViewById(R.id.recyclerViewNwes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchEditText = view.findViewById(R.id.editTextSearch)
        btnSearch = view.findViewById(R.id.buttonSearch)

        bottomNav = view.findViewById(R.id.bottomNavigationView)

        fetchTopNews(countryName, categoryName)

        btnSearch.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim()

            if (searchQuery.isNotEmpty()) {
                val action = NewsRecyclerFragmentDirections
                    .actionNewsRecyclerFragmentToSearchResultsFragment(searchQuery)
                findNavController().navigate(action)
            }
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favourites -> {
                    findNavController().navigate(R.id.action_newsRecyclerFragment_to_favouriteHeadlinesFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun fetchTopNews(country: String, category: String) {
        val mainActivity = activity as MainActivity
        val countryCode =
            countryCodeMap[country] ?: "us" // Default to "us" (United States) if country not found

        // Ensure category is correctly passed
        val categoryName = category
        Log.d(
            "fetchTopResult",
            "Fetching news for: $country, Category: $category, CategoryName = $categoryName"
        )  // Log the country and category

        retrofit.getTopNews(
            country = countryCode.toString(),
            category = categoryName.toString(),
            apiKey = API_Key
        )
            .enqueue(object : Callback<NewsResponse> {
                override fun onResponse(
                    call: Call<NewsResponse>,
                    response: Response<NewsResponse>
                ) {
                    // Clear old data
                    NewsTitle.clear()
                    NewsDesc.clear()
                    NewsImage.clear()
                    NewsDate.clear()
                    NewsSource.clear()
                    content.clear()
                    URL.clear()

                    if (response.isSuccessful) {
                        Log.d(
                            "NewsAPI",
                            "Successfully fetched news for $country, Category: $category"
                        )

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

                        // Initialize the adapter for the first time
                        if (!::newsAdapter.isInitialized) {
                            newsAdapter = NewsAdapter(
                                NewsImage, NewsTitle, NewsDesc, NewsDate, NewsSource, content, URL
                            ) { title, desc, imageUrl, date, source, content, url ->
                                val action = NewsRecyclerFragmentDirections
                                    .actionNewsRecyclerFragmentToNewsDetailsFragment(
                                        title = title, desc = desc, imageUrl = imageUrl,
                                        date = date, source = source, content = content, url = url
                                    )
                                findNavController().navigate(action)
                            }

                            // Attach the adapter to the RecyclerView
                            recyclerView.adapter = newsAdapter
                            mainActivity.startNotification(requireContext())
                        } else {
                            // If already initialized, just notify the adapter that data has changed
                            newsAdapter.notifyDataSetChanged()
                        }

                    } else {
                        Log.e("NewsRecyclerFragment", "API error: ${response.code()}")
                        Log.e("NewsAPI", "Error fetching news: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    Log.e("NewsRecyclerFragment", "Error fetching news: ${t.message}")
                }
            })
    }

}



