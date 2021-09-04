package com.example.expandablerecyclerviewwithpaging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.models.Source
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import kotlinx.android.synthetic.main.header_row.view.*

class NewsAdapter(val newsSourcesList: MutableList<Source>): RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    var myCallBackInterface: MyCallBackInterface? = null
    inner class NewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(
            R.layout.header_row, parent, false
        )
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        var row = newsSourcesList[position]

        (holder as NewsViewHolder).itemView.apply {
            tv_header.text = row.name
            iv_expand?.setOnClickListener {
                myCallBackInterface?.callBackMethod(row.id, 1)
            }
        }
    }

    override fun getItemCount(): Int = newsSourcesList.size

    fun setCallBackInterface(myCallBackInterface: MyCallBackInterface){
        this.myCallBackInterface = myCallBackInterface
    }

    //TODO
//    fun refreshList(newListContents: MutableList<Article>, rowToBeInsertedAt: Int){
//
//    }
}