package com.example.project_skebob

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    internal var dbHelper = DatabaseHelper(application)
    internal var apiService = ApiClient.apiService
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    internal var mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    val allFacts = MutableLiveData<List<FactRecord>>()
    val favoriteFacts = MutableLiveData<List<FactRecord>>()

    val allStatsLiveData = MutableLiveData<String>()
    val favStatsLiveData = MutableLiveData<String>()

    val errorLiveData = MutableLiveData<String>()

    var filterAll = false
    var filterFavs = false

    fun loadAll(filter24: Boolean = filterAll) {
        filterAll = filter24
        viewModelScope.launch(ioDispatcher) {
            val data = dbHelper.getFacts(onlyFavorites = false, filterLast24h = filterAll)
            val stats = dbHelper.getAggregationStats(onlyFavorites = false)
            withContext(mainDispatcher) {
                allFacts.value = data
                allStatsLiveData.value = stats
            }
        }
    }

    fun loadFavorites(filter24: Boolean = filterFavs) {
        filterFavs = filter24
        viewModelScope.launch(ioDispatcher) {
            val data = dbHelper.getFacts(onlyFavorites = true, filterLast24h = filterFavs)
            val stats = dbHelper.getAggregationStats(onlyFavorites = true)
            withContext(mainDispatcher) {
                favoriteFacts.value = data
                favStatsLiveData.value = stats
            }
        }
    }

    fun toggleFavorite(fact: FactRecord) {
        viewModelScope.launch(ioDispatcher) {
            dbHelper.toggleFavorite(fact.id, fact.isFavorite)

            loadAll()
            loadFavorites()
        }
    }

    fun deleteFact(id: Long) {
        viewModelScope.launch(ioDispatcher) {
            dbHelper.deleteFact(id)
            loadAll()
            loadFavorites()
        }
    }


    fun fetchAndSaveNewFact() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val response = apiService.getRandomFact()
                dbHelper.insertFact(response.fact)
                loadAll()
            } catch (e: Exception) {
                withContext(mainDispatcher) {
                    errorLiveData.value = e.message ?: "Unknown error"
                }
            }
        }
    }

}
