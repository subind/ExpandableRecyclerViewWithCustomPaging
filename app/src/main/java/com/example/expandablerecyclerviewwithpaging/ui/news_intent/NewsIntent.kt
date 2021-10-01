package com.example.expandablerecyclerviewwithpaging.ui.news_intent

sealed class NewsIntent {

    object FetchSources: NewsIntent()
    data class FetchArticles(val sourceId: String): NewsIntent()

}