package com.example.expandablerecyclerviewwithpaging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expandablerecyclerviewwithpaging.R
import com.example.expandablerecyclerviewwithpaging.models.ExpandCollapseModel
import com.example.expandablerecyclerviewwithpaging.util.MyCallBackInterface
import kotlinx.android.synthetic.main.child_row.view.*
import kotlinx.android.synthetic.main.header_row.view.*

class NewsAdapter(val newsList: MutableList<ExpandCollapseModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var myCallBackInterface: MyCallBackInterface? = null
    var isAnyRowExpanded = false

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

                    var noOfChildRowsRemoved = 0
                    var lastChildIndexPosition = -1
                    var collapseArrowSwitch = true

                    val iterator = newsList.listIterator()
                    for ((index, value) in iterator.withIndex()) {
                        var type = value.type
                        if(type == ExpandCollapseModel.CHILD){
                            if(collapseArrowSwitch) {
                                newsList[index - 1].isExpanded = false
                                collapseArrowSwitch = false
                            }
                            lastChildIndexPosition = index
                            ++noOfChildRowsRemoved
                            iterator.remove()
                        }
                    }

                    if(position > lastChildIndexPosition){
                        var position = position
                        position -= noOfChildRowsRemoved
                        myCallBackInterface?.callBackMethod(newsList[position].header?.id ?: "", position)
                    }else{
                        myCallBackInterface?.callBackMethod(newsList[position].header?.id ?: "", position)
                    }
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
        isAnyRowExpanded = true
        newsList[rowToBeInsertedAt].isExpanded = true
        newsList.addAll(rowToBeInsertedAt + 1, newListContents)
        notifyDataSetChanged()
    }

    fun collapseRow(rowToBeInsertedAt: Int){
        isAnyRowExpanded = false
        newsList[rowToBeInsertedAt].isExpanded = false

        val iterator = newsList.listIterator()
        for ((index, value) in iterator.withIndex()) {
            var type = value.type
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