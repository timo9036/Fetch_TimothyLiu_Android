package com.example.fetch_timothyliu_android.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.fetch_timothyliu_android.data.model.NetworkItem

class RemoteDataSource(private val apiService: ApiService) {

    suspend fun fetchItems(): List<NetworkItem> {
        return withContext(Dispatchers.IO) {
            apiService.getItems()
        }
    }
}