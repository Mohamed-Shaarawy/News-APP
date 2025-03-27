package com.example.newsapi.api

import com.example.newsapi.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPIService {
    @GET("v2/top-headlines")
    fun getTopNews(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun searchArticles(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ):Call<NewsResponse>

    @GET("v2/top-headlines")
    fun getFavHeadlines(
        @Query("country") country: String,
        @Query("category") sources: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>

}