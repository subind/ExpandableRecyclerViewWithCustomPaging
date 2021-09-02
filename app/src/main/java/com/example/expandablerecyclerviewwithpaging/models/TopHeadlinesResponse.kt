package com.example.expandablerecyclerviewwithpaging.models

data class TopHeadlinesResponse(
    val status: String,
    val totalResults: Int,
    val articles: MutableList<Article>
)