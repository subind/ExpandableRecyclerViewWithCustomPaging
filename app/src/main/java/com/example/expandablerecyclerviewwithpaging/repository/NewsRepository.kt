package com.example.expandablerecyclerviewwithpaging.repository

import com.example.expandablerecyclerviewwithpaging.api.RetrofitInstance

class NewsRepository {

    suspend fun getNewsSources(newsLanguage: String) =
        RetrofitInstance.api.getNewsSources(language = newsLanguage)

    suspend fun getTopHeadlines(source: String, pageNumber: Int) =
        RetrofitInstance.api.getTopHeadlines(sources = source, pageNumber = pageNumber)

}