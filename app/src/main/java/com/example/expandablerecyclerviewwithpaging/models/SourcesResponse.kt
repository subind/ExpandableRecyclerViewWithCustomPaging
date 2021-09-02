package com.example.expandablerecyclerviewwithpaging.models

data class SourcesResponse(
    var status: String,
    var sources: MutableList<Source>
)
