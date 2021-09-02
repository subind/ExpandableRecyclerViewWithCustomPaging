package com.example.expandablerecyclerviewwithpaging.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expandablerecyclerviewwithpaging.NewsApplication
import com.example.expandablerecyclerviewwithpaging.models.Article
import com.example.expandablerecyclerviewwithpaging.models.Source
import com.example.expandablerecyclerviewwithpaging.models.SourcesResponse
import com.example.expandablerecyclerviewwithpaging.models.TopHeadlinesResponse
import com.example.expandablerecyclerviewwithpaging.repository.NewsRepository
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.NEWS_LANGUAGE
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.STATUS_OK
import com.example.expandablerecyclerviewwithpaging.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    val newsSources: MutableLiveData<Resource<SourcesResponse>> = MutableLiveData()
    val newsSourcesList: MutableList<Source> = mutableListOf<Source>()

    val topHeadlineArticles: MutableLiveData<Resource<TopHeadlinesResponse>> = MutableLiveData()
    val topHeadlineArticlesList: MutableList<Article> = mutableListOf<Article>()

    init {
        getNewsSources(NEWS_LANGUAGE)
    }

    fun getNewsSources(language: String) = viewModelScope.launch {
        safeNewsSourcesCall(language)
    }

    private suspend fun safeNewsSourcesCall(language: String) {
        newsSources.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getNewsSources(language)
                newsSources.postValue(handleSourcesResponse(response))
            } else {
                newsSources.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> newsSources.postValue(Resource.Error("Network Failure"))
                else -> newsSources.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleSourcesResponse(response: Response<SourcesResponse>): Resource<SourcesResponse>? {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == STATUS_OK) {
                    newsSourcesList.addAll(it.sources)
                    return Resource.Success(it)
                }
            }
        }
        return Resource.Error(response.message())
    }

    fun getTopHeadlineArticles(source: String, pageNumber: Int) = viewModelScope.launch {
        safeTopHeadlinesArticlesCall(source, pageNumber)
    }

    private suspend fun safeTopHeadlinesArticlesCall(source: String, pageNumber: Int) {
        topHeadlineArticles.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getTopHeadlines(source, pageNumber)
                topHeadlineArticles.postValue(handleTopHeadlinesArticlesResponse(response))
            } else {
                topHeadlineArticles.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> topHeadlineArticles.postValue(Resource.Error("Network Failure"))
                else -> topHeadlineArticles.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleTopHeadlinesArticlesResponse(response: Response<TopHeadlinesResponse>): Resource<TopHeadlinesResponse>? {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == STATUS_OK) {
                    topHeadlineArticlesList.addAll(it.articles)
                    return Resource.Success(it)
                }
            }
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}