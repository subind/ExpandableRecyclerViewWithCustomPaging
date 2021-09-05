package com.example.expandablerecyclerviewwithpaging.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.adapter.NewsAdapter
import com.example.expandablerecyclerviewwithpaging.models.ExpandCollapseModel
import com.example.expandablerecyclerviewwithpaging.models.Source
import com.example.expandablerecyclerviewwithpaging.repository.NewsRepository
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import com.example.expandablerecyclerviewwithpaging.util.Resource
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.item_error_message.*

class NewsActivity : AppCompatActivity(), MyCallBackInterface {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val newsRepository = NewsRepository()
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setUpRecyclerView(mutableListOf<ExpandCollapseModel>())

        viewModel.newsSources.observe(this, Observer {
            when(it){
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    setUpRecyclerView(viewModel.prepareSourcesDataForExpandableAdapter(it.data?.sources ?: mutableListOf<Source>()))
                }
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { message ->
                        Toast.makeText(this, "An error occurred: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        viewModel.topHeadlines.observe(this, Observer {
            when(it){
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    it.data?.let {
                        val topHeadlinesList = it.articles
                        newsAdapter.expandRow(viewModel.prepareTopHeadlinesDataForExpandableAdapter(topHeadlinesList), viewModel.rowPositionTracker)
                        val totalPages = it.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.topHeadlinesPageNumber == totalPages
                        if(isLastPage) {
                            Toast.makeText(this, "Final page loaded", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { message ->
                        Toast.makeText(this, "An error occurred: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = viewModel.loadedChildCount>=viewModel.totalChildCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && !isAtLastItem && isNotAtBeginning && isScrolling
            if(shouldPaginate) {
                viewModel.getTopHeadlineArticles(viewModel.sourceIdTracker ?: "")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = message
        isError = true
    }

    private fun setUpRecyclerView(newsList: MutableList<ExpandCollapseModel>) {
        newsAdapter = NewsAdapter(newsList)
        newsAdapter.let {
            it.setCallBackInterface(this)
            news_rv.apply {
                adapter = it
                layoutManager = LinearLayoutManager(this@NewsActivity)
                addOnScrollListener(scrollListener)
            }
        }
    }

    override fun callBackMethod(sourceId: String, rowPosition: Int) {
        viewModel.loadedChildCount = 0
        viewModel.totalChildCount = -1

        viewModel.topHeadlinesPageNumber = 1
        viewModel.sourceIdTracker = sourceId
        viewModel.rowPositionTracker = rowPosition
        viewModel.getTopHeadlineArticles(sourceId)
    }

}