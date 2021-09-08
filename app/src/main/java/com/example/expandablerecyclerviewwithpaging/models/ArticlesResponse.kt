package com.example.expandablerecyclerviewwithpaging.models

data class ArticlesResponse(
    val status: String,
    val totalResults: Int,
    val articles: MutableList<Article>
)