package com.example.newsapi

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
import com.example.newsapi.api.NewsAPIService
import com.example.newsapi.model.NewsResponse
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

    private var NewsImage = ArrayList<String?>()
    private var NewsTitle = ArrayList<String>()
    private var NewsDesc = ArrayList<String>()
    private var NewsDate = ArrayList<String>()
    private var NewsSource = ArrayList<String>()

    lateinit var searchEditText: EditText;
    lateinit var searchButton: Button;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewNwes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchEditText = view.findViewById(R.id.editTextSearch)
        searchButton = view.findViewById(R.id.buttonSearch)

        retrofit.getTopNews(country = "us", apiKey = API_Key)
            .enqueue(object : Callback<NewsResponse> {
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
                        Log.e(
                            "NewsRecyclerFragment",
                            "Error body: ${response.errorBody()?.string()}"
                        )
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



        searchButton.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim()

            if (searchQuery.isNotEmpty()) {
                val action = NewsRecyclerFragmentDirections
                    .actionNewsRecyclerFragmentToSearchResultsFragment(searchQuery)
                findNavController().navigate(action)
            }
        }


    }

}
