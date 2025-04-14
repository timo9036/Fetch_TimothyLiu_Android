package com.example.fetch_timothyliu_android.data.local

import androidx.lifecycle.LiveData
import com.example.fetch_timothyliu_android.data.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocalDataSource(private val itemDao: ItemDao) {

    fun getItems(): LiveData<List<Item>> = itemDao.getItemsSorted()

    suspend fun saveItems(items: List<Item>) {
        withContext(Dispatchers.IO) {
            itemDao.insertAll(items)
        }
    }

    suspend fun clearItems() {
        withContext(Dispatchers.IO) {
            itemDao.deleteAll()
        }
    }
}