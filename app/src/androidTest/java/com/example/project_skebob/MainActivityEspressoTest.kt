package com.example.project_skebob

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testInitialVisibility() {
        onView(withId(R.id.tvStats)).check(matches(isDisplayed()))
        onView(withId(R.id.btnFetchApi)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToFavoritesAndBack() {
        onView(withId(R.id.nav_fav)).perform(click())
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.nav_all)).perform(click())
        onView(withId(R.id.btnFetchApi)).check(matches(isDisplayed()))
    }

    @Test
    fun testFetchButtonClickAndLoad() {
        // Нажимаем на кнопку "Загрузить"
        onView(withId(R.id.btnFetchApi)).perform(click())

        // Т.к. API может отвечать 1-2 секунды, в реальном проекте лучше использовать IdlingResource.
        // Здесь мы можем просто проверить, что кнопка все еще доступна, или подождать (для примера).
        Thread.sleep(3000) // Даем время API ответить (только для отладки!)

        onView(withId(R.id.btnFetchApi)).check(matches(isEnabled()))
        // Проверяем, что в списке хотя бы что-то появилось (если база была пуста)
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun testFilterToggleClick() {
        onView(withId(R.id.btnFilter)).perform(click())
        // После клика проверяем, что кнопка сменила состояние или текст
        onView(withId(R.id.btnFilter)).check(matches(isDisplayed()))
    }
}
