package com.example.newsapi.model

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsPost>
)