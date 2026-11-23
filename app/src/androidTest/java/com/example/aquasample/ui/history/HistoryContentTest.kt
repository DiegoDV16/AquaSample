package com.example.aquasample.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.theme.AquaSampleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraLaMuestraDelHistorial() {
        val summary = SampleSummary(
            id = "#M-0001",
            location = "Línea L-03 · Tren 02",
            dateLabel = "23/11/2026 10:30",
            timestamp = 1_763_810_000_000,
            status = SampleStatus.VALIDADO,
            count = 30,
            averageCalibre = 36.4,
            synced = true,
            operator = "Francisca Rojas"
        )
        val state = HistoryUiState(
            isLoading = false,
            items = listOf(summary),
            totalCount = 1
        )

        composeRule.setContent {
            AquaSampleTheme {
                HistoryContent(
                    state = state,
                    canCreateSample = false,
                    onExportClick = {},
                    onQueryChange = {},
                    onStatusChange = {},
                    onPeriodChange = {},
                    onCentroChange = {},
                    onTrenChange = {},
                    onClearFilters = {},
                    onNavigateToNewSample = {},
                    onOpenSample = {},
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithText("#M-0001").assertIsDisplayed()
        composeRule.onNodeWithText("Línea L-03 · Tren 02").assertIsDisplayed()
    }
}
