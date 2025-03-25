package com.example.newsapi.Repository

import com.example.newsapi.api.NewsAPIService
import com.example.newsapi.model.NewsResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsRepository {
    private val retrofit = Retrofit.Builder().baseUrl("https://newsapi.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val newsAPI: NewsAPIService = retrofit.create(NewsAPIService::class.java)


    fun getTopNews(country: String, apiKey: String): Call<NewsResponse> {
        return newsAPI.getTopNews(country, apiKey)
    }

}