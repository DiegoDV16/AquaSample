package com.example.aquasample.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _isDarkTheme = MutableStateFlow<Boolean?>(false)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme(isSystemDark: Boolean) {
        val current = _isDarkTheme.value ?: false
        _isDarkTheme.value = !current
        _isDarkMode.value = !current
    }

    fun toggleDarkMode() {
        val current = _isDarkMode.value
        _isDarkMode.value = !current
        _isDarkTheme.value = !current
    }

    fun setDarkTheme(isDark: Boolean?) {
        _isDarkTheme.value = isDark ?: false
        _isDarkMode.value = isDark ?: false
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        _isDarkTheme.value = enabled
    }
}
