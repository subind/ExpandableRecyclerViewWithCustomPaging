package com.example.expandablerecyclerviewwithpaging.ui.news_state

import com.example.expandablerecyclerviewwithpaging.models.ArticlesResponse
import com.example.expandablerecyclerviewwithpaging.models.SourcesResponse
import com.example.expandablerecyclerviewwithpaging.util.Resource

sealed class NewsState{

    object Idle: NewsState()
    object Loading: NewsState()
    data class Error(val error: String): NewsState()
    data class NewsSources(val sources: Resource<SourcesResponse>?): NewsState()
    data class NewsArticles(val articles: Resource<ArticlesResponse>?): NewsState()

}
