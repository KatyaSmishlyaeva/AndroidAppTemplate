package com.softteco.template

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.profile.ProfileRepository
import com.softteco.template.navigation.Graph
import com.softteco.template.ui.AppContent
import com.softteco.template.ui.feature.settings.PreferencesKeys
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @SuppressLint("FlowOperatorInvokedInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val theme = dataStore.data.map {
                it[PreferencesKeys.THEME_MODE]
            }.collectAsState(initial = ThemeMode.SystemDefault.value)
            val appThemeContent: @Composable () -> Unit = {
                var isUserLoggedIn by rememberSaveable { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    isUserLoggedIn = profileRepository.getUser() is Result.Success
                }
                isUserLoggedIn?.let {
                    val startDestination = if (it) Graph.BottomBar.route else Graph.Login.route
                    AppContent(startDestination)
                }
            }
            theme.value?.let {
                AppTheme(themeMode = it, content = appThemeContent)
            } ?: AppTheme(themeMode = ThemeMode.SystemDefault.value, content = appThemeContent)
        }
    }
}
