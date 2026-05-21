// ─────────────────────────────────────────────────────────────────────────────
// ViewModelFactory.kt  (updated — add the NEW branches below)
// ─────────────────────────────────────────────────────────────────────────────
package com.mtoanng.datastream.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.ui.alertrules.AlertRulesViewModel          // NEW
import com.mtoanng.datastream.ui.alerts.AlertsViewModel
import com.mtoanng.datastream.ui.export.ExportViewModel                   // NEW
import com.mtoanng.datastream.ui.history.HistoryViewModel                 // NEW
import com.mtoanng.datastream.ui.home.HomeViewModel
import com.mtoanng.datastream.ui.login.LoginViewModel
import com.mtoanng.datastream.ui.paged.PagedAlertsViewModel               // NEW
import com.mtoanng.datastream.ui.paged.PagedRecommendationsViewModel      // NEW
import com.mtoanng.datastream.ui.pillars.PillarsViewModel
import com.mtoanng.datastream.ui.recommendations.RecommendationsViewModel
import com.mtoanng.datastream.ui.settings.SettingsViewModel

/**
 * Hand-rolled factory wiring repos → ViewModels.
 * All new ViewModels are added at the bottom of the 'when' expression.
 */
class ViewModelFactory(private val app: DataStreamApp) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {

        // ── Existing ViewModels ───────────────────────────────────────────────

        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(app.authRepository(), app.appConfig, app::rebuildApi) as T

        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(app.securityRepository(), app.appConfig) as T

        modelClass.isAssignableFrom(PillarsViewModel::class.java) ->
            PillarsViewModel(app.pillarRepository()) as T

        modelClass.isAssignableFrom(AlertsViewModel::class.java) ->
            AlertsViewModel(app.alertRepository()) as T

        modelClass.isAssignableFrom(RecommendationsViewModel::class.java) ->
            RecommendationsViewModel(app.recommendationRepository()) as T

        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(
                app.authRepository(),
                app.securityRepository(),
                app.appConfig,
                app::rebuildApi,
            ) as T

        // ── NEW ViewModels ────────────────────────────────────────────────────

        /** Alert Rules CRUD screen. */
        modelClass.isAssignableFrom(AlertRulesViewModel::class.java) ->
            AlertRulesViewModel(app.alertRuleRepository()) as T

        /** Fuel-price + grid-load history charts. */
        modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
            HistoryViewModel(app.historyRepository()) as T

        /** Filtered + paginated Alerts list (infinite scroll). */
        modelClass.isAssignableFrom(PagedAlertsViewModel::class.java) ->
            PagedAlertsViewModel(app.pagedRepository()) as T

        /** Filtered + paginated Recommendations list (infinite scroll). */
        modelClass.isAssignableFrom(PagedRecommendationsViewModel::class.java) ->
            PagedRecommendationsViewModel(app.pagedRepository()) as T

        /** Async CSV / PDF export flow. */
        modelClass.isAssignableFrom(ExportViewModel::class.java) ->
            ExportViewModel(app.exportRepository()) as T

        else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
    }
}
