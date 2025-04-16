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

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    val items: LiveData<List<Item>> = repository.items

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
             _errorMessage.value = null
            try {
                repository.refreshItems()
                _errorMessage.postValue(null)
            } catch (networkError: IOException) {
                val currentItems = items.value
                if (currentItems.isNullOrEmpty()) {
                    _errorMessage.postValue("Network error and no cached data available.")
                } else {
                    _errorMessage.postValue("Network error: Could not fetch data. Displaying cached data.")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An unexpected error occurred: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun onErrorMessageShown() {
        _errorMessage.value = null
    }
}