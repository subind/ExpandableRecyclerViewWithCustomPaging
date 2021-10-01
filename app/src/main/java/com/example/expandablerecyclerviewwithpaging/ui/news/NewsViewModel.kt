package com.example.expandablerecyclerviewwithpaging.ui.news

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
import com.example.expandablerecyclerviewwithpaging.ui.news_intent.NewsIntent
import com.example.expandablerecyclerviewwithpaging.ui.news_state.NewsState
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.NEWS_LANGUAGE
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.STATUS_OK
import com.example.expandablerecyclerviewwithpaging.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    val newsIntent = Channel<NewsIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<NewsState>(NewsState.Idle)
    val state: StateFlow<NewsState>
        get() = _state

    /**
     * The 'rowPositionTracker' is helpful in keeping track of the position of the last child element
     * that was added, so that the new list of data obtained via paging can be added right beneath
     * the already rendered list.
     */
    var rowPositionTracker: Int = -1
    var newsSourceIdTracker: String? = null
    var newsArticlesPageNumber = 1

    /**
     * The 'loadedArticleChildCount' indicates the no: of child row elements collectively obtained through
     * each paging attempt.
     */
    var loadedArticleChildCount = 0
    var totalArticleChildCount = -1

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            newsIntent.consumeAsFlow().collect {
                when(it){
                    is NewsIntent.FetchSources -> getNewsSources(NEWS_LANGUAGE)
                    is NewsIntent.FetchArticles -> getNewsArticles(it.sourceId)
                }
            }
        }
    }

    fun getNewsSources(language: String) = viewModelScope.launch {
        safeNewsSourcesCall(language)
    }

    private suspend fun safeNewsSourcesCall(language: String) {
        _state.value = NewsState.Loading
        _state.value = try {
            if (hasInternetConnection()) {
                val response = newsRepository.getNewsSources(language)
                NewsState.NewsSources(handleSourcesResponse(response))
            } else {
                NewsState.Error("No internet connection")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> NewsState.Error("Network Failure")
                else -> NewsState.Error("Conversion Error")
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

    fun getNewsArticles(source: String) = viewModelScope.launch {
        safeNewsArticlesCall(source)
    }

    private suspend fun safeNewsArticlesCall(source: String) {
        _state.value = NewsState.Loading
        _state.value = try {
            if (hasInternetConnection()) {
                val response = newsRepository.getNewsArticles(source, newsArticlesPageNumber)
                NewsState.NewsArticles(handleNewsArticlesResponse(response))
            } else {
                NewsState.Error("No internet connection")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> NewsState.Error("Network Failure")
                else -> NewsState.Error("Conversion Error")
            }
        }
    }

    private fun handleNewsArticlesResponse(response: Response<ArticlesResponse>): Resource<ArticlesResponse>? {
        if (response.isSuccessful) {
            response.body()?.let {
                totalArticleChildCount = it.totalResults
                if(loadedArticleChildCount != 0){
                    rowPositionTracker += it.articles.size
                }
                loadedArticleChildCount += it.articles.size

                newsArticlesPageNumber++
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

    /**
     * The below methods "prepareSourcesDataForExpandableAdapter" & "prepareTopHeadlinesDataForExpandableAdapter"
     * create a list of ExpandCollapseModel type, since the NewsAdapter class deals with a single list of type
     * ExpandCollapseModel, that has properties to distinguish header, child also whether the row is expanded.
     */
    fun prepareSourcesDataForExpandableAdapter(sourcesList: MutableList<Source>): MutableList<ExpandCollapseModel>{
        var expandableNewsList = mutableListOf<ExpandCollapseModel>()
        for (sources in sourcesList) {
            var expandableModel = ExpandCollapseModel()
            expandableModel.type = ExpandCollapseModel.SOURCE_HEADER
            expandableModel.sourceHeader = sources
            expandableModel.articleChild = null
            expandableNewsList.add(expandableModel)
        }
        return expandableNewsList
    }

    fun prepareNewsArticlesDataForExpandableAdapter(topHeadlinesList: MutableList<Article>): MutableList<ExpandCollapseModel>{
        var expandableNewsList = mutableListOf<ExpandCollapseModel>()
        for (topHeadlines in topHeadlinesList) {
            var expandableModel = ExpandCollapseModel()
            expandableModel.type = ExpandCollapseModel.ARTICLE_CHILD
            expandableModel.articleChild = topHeadlines
            expandableModel.sourceHeader = null
            expandableNewsList.add(expandableModel)
        }
        return expandableNewsList
    }

}