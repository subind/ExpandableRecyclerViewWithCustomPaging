package com.example.expandablerecyclerviewwithpaging.ui.news

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.adapter.NewsAdapter
import com.example.expandablerecyclerviewwithpaging.models.ExpandCollapseModel
import com.example.expandablerecyclerviewwithpaging.models.Source
import com.example.expandablerecyclerviewwithpaging.repository.NewsRepository
import com.example.expandablerecyclerviewwithpaging.ui.news_intent.NewsIntent
import com.example.expandablerecyclerviewwithpaging.ui.news_state.NewsState
import com.example.expandablerecyclerviewwithpaging.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.item_error_message.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewsActivity : AppCompatActivity(), MyCallBackInterface {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isChildScrolling = false
    var currentPos = 0
    var scrollToPos = 0
    var expandDummy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val newsRepository = NewsRepository()
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setUpRecyclerView(mutableListOf<ExpandCollapseModel>())

        initialise()

        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is NewsState.NewsSources -> {
                        hideProgressBar()
                        hideErrorMessage()
                        setUpRecyclerView(
                            viewModel.prepareSourcesDataForExpandableAdapter(
                                it.sources?.data?.sources ?: mutableListOf<Source>()
                            )
                        )
                    }
                    is NewsState.NewsArticles -> {
                        hideProgressBar()
                        hideErrorMessage()
                        it.articles?.data?.let {
                            val topHeadlinesList = it.articles
                            newsAdapter.expandRow(viewModel.prepareNewsArticlesDataForExpandableAdapter(topHeadlinesList), viewModel.rowPositionTracker)
                            if(scrollToPos > 0 && expandDummy){
                                news_rv.scrollToPosition(scrollToPos)
                                expandDummy = false
                                Log.i("subind", "scrolledTo: $scrollToPos")
                            }
                            /**
                             * The below formula is used to determine the no: of pages to paginate,
                             * here "QUERY_PAGE_SIZE" is the constant that we sent to the api to get the
                             * number of topHeadline articles in each request, whereas "totalResults"
                             * depicts the no: of art topHeadline articles available.
                             */
                            val totalPages = it.totalResults / QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.newsArticlesPageNumber == totalPages
                            if(isLastPage) {
                                newsAdapter.isAnyRowExpanded = false
                                Toast.makeText(this@NewsActivity, "Final page loaded", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    is NewsState.Error -> {
                        hideProgressBar()
                        it.error.let { message ->
                            Toast.makeText(this@NewsActivity, "An error occurred: $message", Toast.LENGTH_LONG)
                                .show()
                            showErrorMessage(message)
                        }
                    }
                    is NewsState.Loading -> {
                        showProgressBar()
                    }
                }
            }
        }
    }

    private fun initialise() {
        lifecycleScope.launch {
            viewModel.newsIntent.send(NewsIntent.FetchSources)
        }
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
            val isAtLastItem = viewModel.loadedArticleChildCount>=viewModel.totalArticleChildCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && !isAtLastItem && isNotAtBeginning && isChildScrolling
            if(shouldPaginate) {
                lifecycleScope.launch {
                    viewModel.newsIntent.send(NewsIntent.FetchArticles(viewModel.newsSourceIdTracker ?: ""))
                }
                isChildScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                /**
                 * "isChildScrolling" is used to determine whether to paginate or not.
                 * The "isAnyRowExpanded" (explained in NewsAdapter class)
                 */
                if(newsAdapter.isAnyRowExpanded) {
                    isChildScrolling = true
                }
            }
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                currentPos = (news_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                Log.i("subind", "currentPos: $currentPos")
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
        viewModel.loadedArticleChildCount = 0
        viewModel.totalArticleChildCount = -1

        viewModel.newsArticlesPageNumber = 1
        viewModel.newsSourceIdTracker = sourceId
        viewModel.rowPositionTracker = rowPosition
        lifecycleScope.launch {
            viewModel.newsIntent.send(NewsIntent.FetchArticles(sourceId))
        }
    }

    override fun noOfItemsRemoved(number: Int, expandDummy: Boolean) {
        Log.i("subind", "number: $number")
        scrollToPos = currentPos - number
        this.expandDummy = expandDummy
        Log.i("subind", "scrollToPos: $scrollToPos, expandDummy: $expandDummy")
    }


}