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
import com.example.expandablerecyclerviewwithpaging.models.*
import com.example.expandablerecyclerviewwithpaging.repository.NewsRepository
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
    val topHeadlines: MutableLiveData<Resource<TopHeadlinesResponse>> = MutableLiveData()

    var rowPositionTracker: Int = -1
    var sourceIdTracker: String? = null
    var topHeadlinesPageNumber = 1

    var loadedChildCount = 0
    var totalChildCount = -1

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
                    return Resource.Success(it)
                }
            }
        }
        return Resource.Error(response.message())
    }

    fun getTopHeadlineArticles(source: String) = viewModelScope.launch {
        safeTopHeadlinesArticlesCall(source)
    }

    private suspend fun safeTopHeadlinesArticlesCall(source: String) {
        topHeadlines.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getTopHeadlines(source, topHeadlinesPageNumber)
                topHeadlines.postValue(handleTopHeadlinesArticlesResponse(response))
            } else {
                topHeadlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> topHeadlines.postValue(Resource.Error("Network Failure"))
                else -> topHeadlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleTopHeadlinesArticlesResponse(response: Response<TopHeadlinesResponse>): Resource<TopHeadlinesResponse>? {
        if (response.isSuccessful) {
            response.body()?.let {
                totalChildCount = it.totalResults
                rowPositionTracker += loadedChildCount
                loadedChildCount += it.articles.size

                topHeadlinesPageNumber++
                if (it.status == STATUS_OK) {
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

    fun prepareSourcesDataForExpandableAdapter(sourcesList: MutableList<Source>): MutableList<ExpandCollapseModel>{
        var expandableNewsList = mutableListOf<ExpandCollapseModel>()
        for (sources in sourcesList) {
            var expandableModel = ExpandCollapseModel()
            expandableModel.type = ExpandCollapseModel.HEADER
            expandableModel.header = sources
            expandableModel.child = null
            expandableNewsList.add(expandableModel)
        }
        return expandableNewsList
    }

    fun prepareTopHeadlinesDataForExpandableAdapter(topHeadlinesList: MutableList<Article>): MutableList<ExpandCollapseModel>{
        var expandableNewsList = mutableListOf<ExpandCollapseModel>()
        for (topHeadlines in topHeadlinesList) {
            var expandableModel = ExpandCollapseModel()
            expandableModel.type = ExpandCollapseModel.CHILD
            expandableModel.child = topHeadlines
            expandableModel.header = null
            expandableNewsList.add(expandableModel)
        }
        return expandableNewsList
    }

}