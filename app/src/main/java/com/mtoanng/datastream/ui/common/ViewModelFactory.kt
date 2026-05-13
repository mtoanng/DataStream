package com.mtoanng.datastream.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.ui.alerts.AlertsViewModel
import com.mtoanng.datastream.ui.home.HomeViewModel
import com.mtoanng.datastream.ui.login.LoginViewModel
import com.mtoanng.datastream.ui.pillars.PillarsViewModel
import com.mtoanng.datastream.ui.recommendations.RecommendationsViewModel
import com.mtoanng.datastream.ui.settings.SettingsViewModel

/**
 * Hand-rolled factory wiring repos -> ViewModels. Avoids Hilt/Dagger to keep the
 * project beginner-friendly.
 */
class ViewModelFactory(private val app: DataStreamApp) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
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
        else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
    }
}
