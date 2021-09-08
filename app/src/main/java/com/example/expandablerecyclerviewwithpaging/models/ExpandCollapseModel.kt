package com.example.expandablerecyclerviewwithpaging.models

/**
 * A custom model that helps us create a single list of this type.
 * That has properties which enables us to distinguish between Header & Child rows
 */
class ExpandCollapseModel {

    companion object{
        const val SOURCE_HEADER = 1
        const val ARTICLE_CHILD = 2
    }

    var type: Int = 0
    var isExpanded: Boolean = false
    var sourceHeader: Source? = null
    var articleChild: Article? = null

}