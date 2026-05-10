package com.example.project_skebob

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class MainActivityUiAutomatorTest {

    private lateinit var device: UiDevice
    private val packageName = "com.example.project_skebob"

    @Before
    fun setUp() {
        // Инициализация UiDevice
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Переход на главный экран
        device.pressHome()

        // Ожидание лончера
        val launcherPackage: String = device.launcherPackageName
        assertNotNull(launcherPackage)
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000)

        // Запуск приложения
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Ожидание появления приложения
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
    }

    @Test
    fun testAppLaunchAndVisibility() {
        // Проверка наличия кнопки через UI Automator (поиск по ID ресурса)
        val fetchButton = device.findObject(By.res(packageName, "btnFetchApi"))
        assertNotNull("Кнопка 'Загрузить' должна быть видна", fetchButton)
    }

    @Test
    fun testOrientationChange() {
        // Поворот в альбомную ориентацию (Landscape)
        device.setOrientationLeft()

        // Ждем появления кнопки фильтра в новом макете (до 5 секунд)
        // Until.hasObject возвращает true, если объект появился
        val isFound = device.wait(Until.hasObject(By.res(packageName, "btnFilter")), 5000)

        assertNotNull("Кнопка фильтра не найдена после поворота экрана в Landscape. " +
                "Проверьте, что в layout-land/fragment_list.xml ID кнопки совпадает с @id/btnFilter",
            if (isFound) true else null)

        // Дополнительно можно проверить, что кнопка видна на экране
        val filterButton = device.findObject(By.res(packageName, "btnFilter"))
        assertNotNull(filterButton)

        // Возврат в обычную ориентацию (Portrait)
        device.setOrientationNatural()

        // Снова ждем появления, так как Activity опять пересоздается
        device.wait(Until.hasObject(By.res(packageName, "btnFilter")), 5000)
    }
}
