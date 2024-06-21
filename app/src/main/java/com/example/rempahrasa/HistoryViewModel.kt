package com.example.rempahrasa

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _historyItems = MutableLiveData<List<HistoryItem>>()
    val historyItems: LiveData<List<HistoryItem>> = _historyItems

    fun fetchHistoryItems() {
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)
                if (token != null) {
                    val response = RetrofitInstance.api.getHistories("Bearer $token")
                    if (response.isSuccessful) {
                        _historyItems.value = response.body()?.data ?: emptyList()
                    } else {
                        // Handle API error
                    }
                } else {
                    // Handle missing token
                }
            } catch (e: Exception) {
                // Handle fetch error
            }
        }
    }
}
