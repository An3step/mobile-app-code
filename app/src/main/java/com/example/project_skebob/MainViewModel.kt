package com.example.project_skebob

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    val allFacts = MutableLiveData<List<FactRecord>>()
    val favoriteFacts = MutableLiveData<List<FactRecord>>()

    val allStatsLiveData = MutableLiveData<String>()
    val favStatsLiveData = MutableLiveData<String>()

    val errorLiveData = MutableLiveData<String>()

    var filterAll = false
    var filterFavs = false

    fun loadAll(filter24: Boolean = filterAll) {
        filterAll = filter24
        viewModelScope.launch(Dispatchers.IO) {
            val data = dbHelper.getFacts(onlyFavorites = false, filterLast24h = filterAll)
            val stats = dbHelper.getAggregationStats(onlyFavorites = false)
            withContext(Dispatchers.Main) {
                allFacts.value = data
                allStatsLiveData.value = stats
            }
        }
    }

    fun loadFavorites(filter24: Boolean = filterFavs) {
        filterFavs = filter24
        viewModelScope.launch(Dispatchers.IO) {
            val data = dbHelper.getFacts(onlyFavorites = true, filterLast24h = filterFavs)
            val stats = dbHelper.getAggregationStats(onlyFavorites = true)
            withContext(Dispatchers.Main) {
                favoriteFacts.value = data
                favStatsLiveData.value = stats
            }
        }
    }

    fun toggleFavorite(fact: FactRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.toggleFavorite(fact.id, fact.isFavorite)

            loadAll()
            loadFavorites()
        }
    }

    fun deleteFact(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteFact(id)
            loadAll()
            loadFavorites()
        }
    }


    fun fetchAndSaveNewFact() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getRandomFact()
                dbHelper.insertFact(response.fact)
                loadAll()
            } catch (e: Exception) {

            }
        }
    }

}