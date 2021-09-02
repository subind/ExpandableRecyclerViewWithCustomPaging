package com.example.expandablerecyclerviewwithpaging.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.adapter.NewsAdapter
import com.example.expandablerecyclerviewwithpaging.repository.NewsRepository
import com.example.expandablerecyclerviewwithpaging.util.Resource
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.item_error_message.*

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var isError = false
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val newsRepository = NewsRepository()
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setUpRecyclerView()

        viewModel.newsSources.observe(this, Observer {
            when(it){
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    setUpRecyclerView()
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

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter(viewModel.newsSourcesList)
        news_rv.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@NewsActivity)
        }
    }

}