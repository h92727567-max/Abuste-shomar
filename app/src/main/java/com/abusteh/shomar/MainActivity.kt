package com.abusteh.shomar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abusteh.shomar.data.database.AppDatabase
import com.abusteh.shomar.data.repository.GameRepository
import com.abusteh.shomar.data.settings.AppSettings
import com.abusteh.shomar.ui.screens.GameScreen
import com.abusteh.shomar.ui.screens.GamesHistoryScreen
import com.abusteh.shomar.ui.screens.HistoryScreen
import com.abusteh.shomar.ui.screens.HomeScreen
import com.abusteh.shomar.ui.screens.NewGameScreen
import com.abusteh.shomar.ui.screens.RoundScreen
import com.abusteh.shomar.ui.screens.SettingsScreen
import com.abusteh.shomar.ui.theme.AbustehShomarTheme
import com.abusteh.shomar.viewmodel.GameViewModel
import com.abusteh.shomar.viewmodel.HistoryViewModel
import com.abusteh.shomar.viewmodel.SettingsViewModel

/**
 * فعالیت اصلی برنامه.
 *
 * برنامه همیشه راست‌چین (RTL) نمایش داده می‌شود، صرف‌نظر از زبان سیستم،
 * چون تمام محتوای برنامه فارسی است.
 *
 * از Phase 7 به بعد، GameViewModel یک [GameRepository] واقعی (روی Room)
 * دریافت می‌کند تا بازی و تاریخچه دورها به‌صورت دائمی ذخیره شوند.
 * از Phase 10، HistoryViewModel هم روی همان Repository برای تاریخچه
 * بازی‌های قبلی ساخته می‌شود. از Phase 11، SettingsViewModel هم برای
 * تنظیمات برنامه (روی AppSettings/SharedPreferences + همان Repository) اضافه شد.
 */
class MainActivity : ComponentActivity() {

    private object Routes {
        const val HOME = "home"
        const val NEW_GAME = "new_game"
        const val GAME = "game"
        const val ROUND = "round"
        const val ROUND_HISTORY = "round_history"
        const val GAMES_HISTORY = "games_history"
        const val GAME_DETAILS = "game_details"
        const val SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AbustehShomarTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val context = LocalContext.current
                        val repository = remember {
                            val database = AppDatabase.getInstance(context.applicationContext)
                            GameRepository(database.gameDao(), database.roundDao())
                        }
                        val appSettings = remember { AppSettings(context.applicationContext) }

                        val gameViewModel: GameViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { GameViewModel(repository) }
                            }
                        )
                        val historyViewModel: HistoryViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { HistoryViewModel(repository) }
                            }
                        )
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { SettingsViewModel(appSettings, repository) }
                            }
                        )

                        val navController = rememberNavController()

                        AppNavHost(
                            navController = navController,
                            gameViewModel = gameViewModel,
                            historyViewModel = historyViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun AppNavHost(
        navController: NavHostController,
        gameViewModel: GameViewModel,
        historyViewModel: HistoryViewModel,
        settingsViewModel: SettingsViewModel
    ) {
        val currentGame by gameViewModel.game.collectAsState()
        val currentRounds by gameViewModel.rounds.collectAsState()
        val hasSavedGame by gameViewModel.hasSavedGame.collectAsState()

        val finishedGames by historyViewModel.finishedGames.collectAsState()
        val selectedGame by historyViewModel.selectedGame.collectAsState()
        val selectedGameRounds by historyViewModel.selectedGameRounds.collectAsState()

        val roundConfirmationEnabled by settingsViewModel.roundConfirmationEnabled.collectAsState()

        // مدیریت خطا (بخش ۲۶ مشخصات): پیام‌های خطای ساده فارسی از هر سه
        // ViewModel، در یک نوار Snackbar مشترک در پایین صفحه نشان داده می‌شوند.
        val snackbarHostState = remember { SnackbarHostState() }

        val gameError by gameViewModel.errorMessage.collectAsState()
        val historyError by historyViewModel.errorMessage.collectAsState()
        val settingsError by settingsViewModel.errorMessage.collectAsState()

        LaunchedEffect(gameError) {
            gameError?.let {
                snackbarHostState.showSnackbar(it)
                gameViewModel.clearError()
            }
        }
        LaunchedEffect(historyError) {
            historyError?.let {
                snackbarHostState.showSnackbar(it)
                historyViewModel.clearError()
            }
        }
        LaunchedEffect(settingsError) {
            settingsError?.let {
                snackbarHostState.showSnackbar(it)
                settingsViewModel.clearError()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(outerPadding)
        ) {

            composable(Routes.HOME) {
                LaunchedEffect(Unit) {
                    gameViewModel.refreshSavedGameStatus()
                }
                HomeScreen(
                    hasSavedGame = hasSavedGame,
                    onNewGameClick = { navController.navigate(Routes.NEW_GAME) },
                    onContinueGameClick = {
                        gameViewModel.continueGame()
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onHistoryClick = { navController.navigate(Routes.GAMES_HISTORY) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.NEW_GAME) {
                NewGameScreen(
                    onGameStarted = { teamOneName, teamTwoName ->
                        gameViewModel.startNewGame(teamOneName, teamTwoName)
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            composable(Routes.GAME) {
                val game = currentGame
                if (game != null) {
                    GameScreen(
                        game = game,
                        rounds = currentRounds,
                        onRegisterRoundClick = { navController.navigate(Routes.ROUND) },
                        onUndoLastRoundClick = { gameViewModel.undoLastRound() },
                        onHistoryClick = { navController.navigate(Routes.ROUND_HISTORY) },
                        onStartNewGameClick = {
                            navController.navigate(Routes.NEW_GAME) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    )
                }
            }

            composable(Routes.ROUND) {
                val game = currentGame
                if (game != null) {
                    RoundScreen(
                        game = game,
                        confirmBeforeRegister = roundConfirmationEnabled,
                        onConfirmRound = { readerTeam, bid, successful, kot ->
                            gameViewModel.registerRound(readerTeam, bid, successful, kot)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.ROUND_HISTORY) {
                val game = currentGame
                if (game != null) {
                    HistoryScreen(
                        teamOneName = game.teamOneName,
                        teamTwoName = game.teamTwoName,
                        rounds = currentRounds
                    )
                }
            }

            composable(Routes.GAMES_HISTORY) {
                LaunchedEffect(Unit) {
                    historyViewModel.loadFinishedGames()
                }
                GamesHistoryScreen(
                    games = finishedGames,
                    onGameClick = { gameSummary ->
                        historyViewModel.selectGame(gameSummary)
                        navController.navigate(Routes.GAME_DETAILS)
                    }
                )
            }

            composable(Routes.GAME_DETAILS) {
                val gameSummary = selectedGame
                if (gameSummary != null) {
                    HistoryScreen(
                        teamOneName = gameSummary.teamOneName,
                        teamTwoName = gameSummary.teamTwoName,
                        rounds = selectedGameRounds
                    )
                }
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    currentGame = currentGame,
                    roundConfirmationEnabled = roundConfirmationEnabled,
                    onRenameTeams = { teamOneName, teamTwoName ->
                        gameViewModel.renameTeams(teamOneName, teamTwoName)
                    },
                    onToggleRoundConfirmation = { enabled ->
                        settingsViewModel.setRoundConfirmationEnabled(enabled)
                    },
                    onStartNewGameClick = {
                        navController.navigate(Routes.NEW_GAME) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onDeleteHistoryConfirmed = {
                        settingsViewModel.deleteHistory {
                            historyViewModel.loadFinishedGames()
                        }
                    }
                )
            }
        }
        }
    }
}
