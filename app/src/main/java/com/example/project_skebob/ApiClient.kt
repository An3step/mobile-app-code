package com.example.project_skebob

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("fact")
    suspend fun getRandomFact(): ApiFactResponse
}

object ApiClient {
    private const val BASE_URL = "https://catfact.ninja/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}