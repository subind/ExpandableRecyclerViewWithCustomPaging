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

/**
 * Please note:-
 * We are achieving expansion/collapse using a single list of custom model type "ExpandCollapseModel"
 */
class NewsAdapter(val newsList: MutableList<ExpandCollapseModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var myCallBackInterface: MyCallBackInterface? = null

    /**
     * In the NewsActivity we have RV adapters onScrollListener, & since currently we have
     * implemented paging only for child row elements, we need to know the changes in
     * onScrollStateChanged only in this condition (i.e, whenever child rows are visible), & its
     * based on which we identify whether the child row elements are being scrolled through or not.
     */
    var isAnyRowExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ExpandCollapseModel.SOURCE_HEADER -> {
                SourceHeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.header_row, parent, false
                    )
                )
            }
            ExpandCollapseModel.ARTICLE_CHILD -> {
                ArticleChildViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.child_row, parent, false
                    )
                )
            }
            else -> {
                SourceHeaderViewHolder(
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
            ExpandCollapseModel.SOURCE_HEADER -> {
                (holder as SourceHeaderViewHolder).sourceHeaderTitle.text = row.sourceHeader?.name

                /**
                 * The 'isExpanded' property in the ExpandCollapseModel is used to keep track of the
                 * expand & collapse arrow images.
                 */
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

                /**
                 * As per current logic, before expansion of the selected header row, any other
                 * header row which was previously expanded needs to be collapsed.
                 */
                holder.expandArrow.setOnClickListener {

                    var noOfChildRowsRemoved = 0
                    var lastChildIndexPosition = -1
                    var collapseArrowSwitch = true

                    /**
                     * Since we are maintaining a single list that contains both header & child row
                     * data's, we loop through this list & remove all the child row types.
                     */
                    val iterator = newsList.listIterator()
                    for ((index, value) in iterator.withIndex()) {
                        var type = value.type
                        if(type == ExpandCollapseModel.ARTICLE_CHILD){
                            if(collapseArrowSwitch) {
                                newsList[index - 1].isExpanded = false
                                collapseArrowSwitch = false
                            }
                            lastChildIndexPosition = index
                            ++noOfChildRowsRemoved
                            iterator.remove()
                        }
                    }

                    /**
                     * If the newly selected row comes beneath/after the already expanded row, &
                     * since we remove all the child row types from the list, the position of the
                     * row to be expanded obtained from the onBindViewHolder, will not be correct
                     * anymore, hence we are subtracting the no: of child rows removed to get the
                     * clicked header rows position.
                     */
                    if(position > lastChildIndexPosition){
                        var position = position
                        position -= noOfChildRowsRemoved
                        myCallBackInterface?.callBackMethod(newsList[position].sourceHeader?.id ?: "", position)
                    }else{
                        myCallBackInterface?.callBackMethod(newsList[position].sourceHeader?.id ?: "", position)
                    }
                }
                holder.collapseArrow.setOnClickListener {
                    collapseRow(position)
                }
            }
            ExpandCollapseModel.ARTICLE_CHILD -> {
                (holder as ArticleChildViewHolder).articleChildTitle.text = row.articleChild?.title
                holder.articleChildDescription.text = row.articleChild?.description
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
        if(newsList[rowToBeInsertedAt].type == ExpandCollapseModel.SOURCE_HEADER) {
            newsList[rowToBeInsertedAt].isExpanded = true
        }
        newsList.addAll(rowToBeInsertedAt + 1, newListContents)
        notifyDataSetChanged()
    }

    fun collapseRow(rowToBeInsertedAt: Int){
        isAnyRowExpanded = false
        newsList[rowToBeInsertedAt].isExpanded = false

        val iterator = newsList.listIterator()
        for ((index, value) in iterator.withIndex()) {
            var type = value.type
            if(type == ExpandCollapseModel.ARTICLE_CHILD){
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }

    class SourceHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var sourceHeaderTitle = itemView.tv_header
        internal var expandArrow = itemView.iv_expand
        internal var collapseArrow = itemView.iv_collapse
    }

    class ArticleChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var articleChildTitle = itemView.tv_child_title
        internal var articleChildDescription = itemView.tv_child_desc
    }

}