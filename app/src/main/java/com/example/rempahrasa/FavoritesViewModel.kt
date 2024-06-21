package com.example.rempahrasa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val _favorites = MutableLiveData<List<FavoriteItem>>()
    val favorites: LiveData<List<FavoriteItem>> get() = _favorites

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("MyAppPrefs", Application.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            viewModelScope.launch {
                try {
                    val response: Response<FavoritesResponse> = RetrofitInstance.api.getFavorites("Bearer $token")
                    if (response.isSuccessful) {
                        _favorites.value = response.body()?.data ?: emptyList()
                    } else {
                        _error.value = response.message()
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        } else {
            _error.value = "Token is null"
        }
    }
}
