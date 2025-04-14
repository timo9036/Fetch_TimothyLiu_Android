package com.example.fetch_timothyliu_android.data.remote

import com.example.fetch_timothyliu_android.data.model.NetworkItem
import retrofit2.http.GET

interface ApiService {
    @GET("hiring.json")
    suspend fun getItems(): List<NetworkItem>
}