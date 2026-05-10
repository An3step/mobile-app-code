package com.example.project_skebob

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testInitialUiState() {
        // Проверяем, что основные элементы экрана видны при запуске
        onView(withId(R.id.tvStats)).check(matches(isDisplayed()))
        onView(withId(R.id.btnFetchApi)).check(matches(isDisplayed()))
        onView(withId(R.id.btnFilter)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToFavorites() {
        // Нажимаем на вкладку "Избранное" в BottomNavigationView
        onView(withId(R.id.nav_fav)).perform(click())

        // Проверяем, что RecyclerView отображается
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun testFilterButtonClick() {
        // Нажимаем на кнопку фильтра
        onView(withId(R.id.btnFilter)).perform(click())
        
        // Проверяем кликабельность
        onView(withId(R.id.btnFilter)).check(matches(isClickable()))
    }

    @Test
    fun testFetchFactAndVerifyAppearance() {
        // 1. Нажимаем на кнопку "Загрузить"
        onView(withId(R.id.btnFetchApi)).perform(click())

        // 2. Ждем ответа от API (имитация ожидания асинхронного ответа)
        // 5 секунд обычно достаточно для получения данных по сети
        Thread.sleep(5000)

        // 3. Проверяем, что текст статистики обновился и больше не содержит дефолтную фразу
        onView(withId(R.id.tvStats)).check(matches(not(withText("Статистика загружается..."))))
        
        // 4. Проверяем, что в RecyclerView появился хотя бы один элемент
        onView(withId(R.id.recyclerView)).check(matches(hasMinimumChildCount(1)))
    }
}
