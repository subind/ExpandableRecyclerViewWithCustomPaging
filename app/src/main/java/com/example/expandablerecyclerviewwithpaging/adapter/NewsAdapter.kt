package com.example.expandablerecyclerviewwithpaging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.models.ExpandCollapseModel
import com.example.expandablerecyclerviewwithpaging.models.Source
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import kotlinx.android.synthetic.main.child_row.view.*
import kotlinx.android.synthetic.main.header_row.view.*

class NewsAdapter(val newsList: MutableList<ExpandCollapseModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var myCallBackInterface: MyCallBackInterface? = null
    inner class NewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ExpandCollapseModel.HEADER -> {
                HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.header_row, parent, false
                    )
                )
            }
            ExpandCollapseModel.CHILD -> {
                ChildViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.child_row, parent, false
                    )
                )
            }
            else -> {
                HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.header_row, parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = newsList[position]
        when (row.type) {
            ExpandCollapseModel.HEADER -> {
                (holder as HeaderViewHolder).headerTitle.text = row.header?.name

                when(row.isExpanded){
                    true -> {
                        holder.expandArrow.visibility = View.GONE
                        holder.collapseArrow.visibility = View.VISIBLE
                    }
                    false -> {
                        holder.collapseArrow.visibility = View.GONE
                        holder.expandArrow.visibility = View.VISIBLE
                    }
                }

                holder.expandArrow.setOnClickListener {
                    collapseRow(position)
                    myCallBackInterface?.callBackMethod(row.header?.id ?: "us", position)
                }
                holder.collapseArrow.setOnClickListener {
                    collapseRow(position)
                }
            }
            ExpandCollapseModel.CHILD -> {
                (holder as ChildViewHolder).childTitle.text = row.child?.title
                holder.childDescription.text = row.child?.description
            }
        }
    }

    override fun getItemCount(): Int = newsList.size

    override fun getItemViewType(position: Int): Int = newsList[position].type

    fun setCallBackInterface(myCallBackInterface: MyCallBackInterface){
        this.myCallBackInterface = myCallBackInterface
    }

    fun expandRow(newListContents: MutableList<ExpandCollapseModel>, rowToBeInsertedAt: Int){
        //Added Refreshed List
        newsList[rowToBeInsertedAt].isExpanded = true
        newsList.addAll(rowToBeInsertedAt + 1, newListContents)
        notifyDataSetChanged()
    }

    fun collapseRow(rowToBeInsertedAt: Int){
        newsList[rowToBeInsertedAt].isExpanded = false
        val iterator = newsList.iterator()
        while(iterator.hasNext()){
            var type = iterator.next().type
            if(type == ExpandCollapseModel.CHILD){
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout = itemView.header_row
        internal var headerTitle = itemView.tv_header
        internal var expandArrow = itemView.iv_expand
        internal var collapseArrow = itemView.iv_collapse
    }

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout = itemView.child_row
        internal var childTitle = itemView.tv_child_title
        internal var childDescription = itemView.tv_child_desc
    }

}