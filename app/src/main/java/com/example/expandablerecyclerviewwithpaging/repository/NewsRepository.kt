package com.example.expandablerecyclerviewwithpaging.repository

import com.example.expandablerecyclerviewwithpaging.api.RetrofitInstance

class NewsRepository {

    suspend fun getNewsSources(newsLanguage: String) =
        RetrofitInstance.api.getNewsSources(language = newsLanguage)

    /**
     * TODO:
     * Temporarily changed the request parameter from "sources" to "country" since sufficient
     * data wasn't available from the server to achieve/test pagination
     */
    suspend fun getNewsArticles(source: String, pageNumber: Int) =
        RetrofitInstance.api.getNewsArticles(/*sources*/country = /*source*/"us", pageNumber = pageNumber)

}