package com.example.expandablerecyclerviewwithpaging.api

import com.example.expandablerecyclerviewwithpaging.models.SourcesResponse
import com.example.expandablerecyclerviewwithpaging.models.TopHeadlinesResponse
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.API_KEY
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.NEWS_LANGUAGE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("/v2/top-headlines/sources")
    suspend fun getNewsSources(
        @Query("apiKey")
        apiKey: String = API_KEY,
        @Query("language")
        language: String = NEWS_LANGUAGE
    ): Response<SourcesResponse>


    /**
     * TODO:
     * Temporarily changed the request parameter from "sources" to "country" since sufficient
     * data wasn't available from the server to achieve/test pagination
     */
    @GET("/v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey")
        apiKey: String = API_KEY,
        /*@Query("sources")
        sources: String*/
        @Query("country")
        country: String,
        @Query("pageSize")
        pageSize: Int = 20,
        @Query("page")
        pageNumber: Int
    ): Response<TopHeadlinesResponse>

}