package com.example.project_skebob

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var dbHelper: DatabaseHelper

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var observer: Observer<List<FactRecord>>

    @Mock
    private lateinit var statsObserver: Observer<String>

    @Mock
    private lateinit var errorObserver: Observer<String>

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = MainViewModel(application)
        viewModel.dbHelper = dbHelper
        viewModel.apiService = apiService
        viewModel.ioDispatcher = testDispatcher
        viewModel.mainDispatcher = testDispatcher
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Positive Tests ---

    @Test
    fun `loadAll sets facts and stats correctly`() = runTest {
        val facts = listOf(FactRecord(1, "Cat fact"))
        val stats = "Records: 1"
        `when`(dbHelper.getFacts(eq(false), eq(false))).thenReturn(facts)
        `when`(dbHelper.getAggregationStats(eq(false))).thenReturn(stats)

        viewModel.allFacts.observeForever(observer)
        viewModel.allStatsLiveData.observeForever(statsObserver)

        viewModel.loadAll(false)
        advanceUntilIdle()

        verify(observer).onChanged(facts)
        verify(statsObserver).onChanged(stats)
        assertEquals(facts, viewModel.allFacts.value)
        assertEquals(stats, viewModel.allStatsLiveData.value)
    }

    @Test
    fun `loadAll with 24h filter sets filterAll state and calls dbHelper`() = runTest {
        `when`(dbHelper.getFacts(eq(false), eq(true))).thenReturn(emptyList())
        
        viewModel.loadAll(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.filterAll)
        verify(dbHelper).getFacts(onlyFavorites = false, filterLast24h = true)
    }

    @Test
    fun `loadFavorites sets facts and stats correctly`() = runTest {
        val facts = listOf(FactRecord(1, "Fav cat fact", isFavorite = true))
        val stats = "Fav Records: 1"
        `when`(dbHelper.getFacts(eq(true), eq(false))).thenReturn(facts)
        `when`(dbHelper.getAggregationStats(eq(true))).thenReturn(stats)

        viewModel.favoriteFacts.observeForever(observer)
        viewModel.favStatsLiveData.observeForever(statsObserver)

        viewModel.loadFavorites(false)
        advanceUntilIdle()

        verify(observer).onChanged(facts)
        verify(statsObserver).onChanged(stats)
        assertEquals(facts, viewModel.favoriteFacts.value)
    }

    @Test
    fun `loadFavorites with 24h filter sets filterFavs state and calls dbHelper`() = runTest {
        `when`(dbHelper.getFacts(eq(true), eq(true))).thenReturn(emptyList())
        
        viewModel.loadFavorites(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.filterFavs)
        verify(dbHelper).getFacts(onlyFavorites = true, filterLast24h = true)
    }

    @Test
    fun `toggleFavorite calls dbHelper and triggers reloads`() = runTest {
        val fact = FactRecord(1, "Fact", isFavorite = false)
        
        viewModel.toggleFavorite(fact)
        advanceUntilIdle()

        verify(dbHelper).toggleFavorite(fact.id, fact.isFavorite)
        // verify reload calls
        verify(dbHelper, atLeastOnce()).getFacts(eq(false), any())
        verify(dbHelper, atLeastOnce()).getFacts(eq(true), any())
    }

    @Test
    fun `deleteFact calls dbHelper and triggers reloads`() = runTest {
        val id = 5L
        
        viewModel.deleteFact(id)
        advanceUntilIdle()

        verify(dbHelper).deleteFact(id)
        verify(dbHelper, atLeastOnce()).getFacts(eq(false), any())
        verify(dbHelper, atLeastOnce()).getFacts(eq(true), any())
    }

    @Test
    fun `fetchAndSaveNewFact successfully fetches and saves`() = runTest {
        val apiResponse = ApiFactResponse("New cat fact", 10)
        `when`(apiService.getRandomFact()).thenReturn(apiResponse)
        
        viewModel.fetchAndSaveNewFact()
        advanceUntilIdle()

        verify(apiService).getRandomFact()
        verify(dbHelper).insertFact("New cat fact")
        verify(dbHelper, atLeastOnce()).getFacts(eq(false), any())
    }

    // --- Negative and Edge Case Tests ---

    @Test
    fun `loadAll when database is empty sets empty list and correct stats`() = runTest {
        `when`(dbHelper.getFacts(any(), any())).thenReturn(emptyList())
        `when`(dbHelper.getAggregationStats(any())).thenReturn("No data")

        viewModel.allFacts.observeForever(observer)
        viewModel.allStatsLiveData.observeForever(statsObserver)
        
        viewModel.loadAll()
        advanceUntilIdle()

        verify(observer).onChanged(emptyList())
        verify(statsObserver).onChanged("No data")
        assertEquals(0, viewModel.allFacts.value?.size)
    }

    @Test
    fun `fetchAndSaveNewFact handles network error`() = runTest {
        val errorMsg = "Network Timeout"
        `when`(apiService.getRandomFact()).thenThrow(RuntimeException(errorMsg))
        
        viewModel.errorLiveData.observeForever(errorObserver)

        viewModel.fetchAndSaveNewFact()
        advanceUntilIdle()

        verify(errorObserver).onChanged(errorMsg)
        assertEquals(errorMsg, viewModel.errorLiveData.value)
    }

    @Test
    fun `fetchAndSaveNewFact handles unknown error (null message)`() = runTest {
        `when`(apiService.getRandomFact()).thenThrow(RuntimeException())
        
        viewModel.errorLiveData.observeForever(errorObserver)

        viewModel.fetchAndSaveNewFact()
        advanceUntilIdle()

        verify(errorObserver).onChanged("Unknown error")
        assertEquals("Unknown error", viewModel.errorLiveData.value)
    }

    @Test
    fun `fetchAndSaveNewFact with empty fact string from API`() = runTest {
        val apiResponse = ApiFactResponse("", 0)
        `when`(apiService.getRandomFact()).thenReturn(apiResponse)
        
        viewModel.fetchAndSaveNewFact()
        advanceUntilIdle()

        verify(dbHelper).insertFact("")
    }

    @Test
    fun `deleteFact also reloads stats for both lists`() = runTest {
        viewModel.deleteFact(99L)
        advanceUntilIdle()

        verify(dbHelper).getAggregationStats(eq(false))
        verify(dbHelper).getAggregationStats(eq(true))
    }

    @Test
    fun `toggleFavorite properly passing parameters to dbHelper`() = runTest {
        val fact = FactRecord(1, "Fav Fact", isFavorite = true)
        
        viewModel.toggleFavorite(fact)
        advanceUntilIdle()

        verify(dbHelper).toggleFavorite(1, true)
    }
}
