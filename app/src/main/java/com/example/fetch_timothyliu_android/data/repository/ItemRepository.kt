package com.example.fetch_timothyliu_android.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.fetch_timothyliu_android.data.local.LocalDataSource
import com.example.fetch_timothyliu_android.data.model.Item
import com.example.fetch_timothyliu_android.data.model.toDatabaseEntityList
import com.example.fetch_timothyliu_android.data.remote.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ItemRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {

    val items: LiveData<List<Item>> = localDataSource.getItems()

    suspend fun refreshItems() {
        withContext(Dispatchers.IO) {
            try {
                val networkItems = remoteDataSource.fetchItems()

                val validItems = networkItems.toDatabaseEntityList()

                localDataSource.clearItems()

                localDataSource.saveItems(validItems)

//                Log.d("ItemRepository", "Items refreshed successfully.")
            } catch (e: IOException) {
//                Log.e("ItemRepository", "Network error refreshing items: ${e.message}", e)
                throw e
            } catch (e: Exception) {
//                Log.e("ItemRepository", "Error refreshing items: ${e.message}", e)
                throw e
            }
        }
    }
}