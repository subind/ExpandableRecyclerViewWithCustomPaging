package com.example.expandablerecyclerviewwithpaging.util

interface MyCallBackInterface {

    fun callBackMethod(sourceId: String, rowPosition: Int)

    fun noOfItemsRemoved(number: Int, expandDummy: Boolean)

}