package com.example.fetch_timothyliu_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.fetch_timothyliu_android.data.local.AppDatabase
import com.example.fetch_timothyliu_android.data.local.LocalDataSource
import com.example.fetch_timothyliu_android.data.model.Item
import com.example.fetch_timothyliu_android.data.remote.RemoteDataSource
import com.example.fetch_timothyliu_android.data.remote.RetrofitInstance
import com.example.fetch_timothyliu_android.data.repository.ItemRepository
import kotlinx.coroutines.launch
import java.io.IOException

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val itemDao = AppDatabase.getDatabase(application).itemDao()
    private val localDataSource = LocalDataSource(itemDao)
    private val remoteDataSource = RemoteDataSource(RetrofitInstance.api)
    private val repository = ItemRepository(remoteDataSource, localDataSource)

    val items: LiveData<List<Item>> = repository.items

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                repository.refreshItems()
            } catch (networkError: IOException) {
                _errorMessage.value = "Network error: Could not fetch data. Displaying cached data if available."
                if (items.value.isNullOrEmpty()) {
                    _errorMessage.value = "Network error and no cached data available."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred: ${e.localizedMessage}"
            } finally {
            }
        }
    }

    fun onErrorMessageShown() {
        _errorMessage.value = null
    }
}