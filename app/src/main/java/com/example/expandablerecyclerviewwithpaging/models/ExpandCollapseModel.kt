package com.example.expandablerecyclerviewwithpaging.models

class ExpandCollapseModel {

    companion object{
        const val SOURCE_HEADER = 1
        const val ARTICLE_CHILD = 2
    }

    var type: Int = 0
    var isExpanded: Boolean = false
    var header: Source? = null
    var child: Article? = null

}