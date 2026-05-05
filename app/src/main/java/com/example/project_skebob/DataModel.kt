package com.example.project_skebob

data class FactRecord(
    val id: Long = 0,
    val factText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class ApiFactResponse(
    val fact: String,
    val length: Int
)