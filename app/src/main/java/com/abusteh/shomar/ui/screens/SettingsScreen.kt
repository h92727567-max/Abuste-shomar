package com.abusteh.shomar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.BuildConfig
import com.abusteh.shomar.R
import com.abusteh.shomar.domain.model.Game
import com.abusteh.shomar.ui.components.ConfirmDialog

/**
 * صفحه تنظیمات (بخش ۱۱ مشخصات):
 * تغییر نام گروه‌های بازی جاری، شروع بازی جدید، فعال/غیرفعال کردن تأیید
 * ثبت دور، حذف تاریخچه، و اطلاعات برنامه.
 */
@Composable
fun SettingsScreen(
    currentGame: Game?,
    roundConfirmationEnabled: Boolean,
    onRenameTeams: (teamOneName: String, teamTwoName: String) -> Unit,
    onToggleRoundConfirmation: (Boolean) -> Unit,
    onStartNewGameClick: () -> Unit,
    onDeleteHistoryConfirmed: () -> Unit
) {
    var showDeleteHistoryDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium
            )

            // --- تغییر نام گروه‌ها ---
            SettingsSection(title = stringResource(R.string.settings_rename_teams_title)) {
                if (currentGame == null) {
                    Text(
                        text = stringResource(R.string.settings_no_active_game),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    RenameTeamsForm(currentGame = currentGame, onRenameTeams = onRenameTeams)
                }
            }

            HorizontalDivider()

            // --- شروع بازی جدید ---
            SettingsSection(title = stringResource(R.string.settings_new_game_title)) {
                OutlinedButton(
                    onClick = onStartNewGameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.btn_new_game))
                }
            }

            HorizontalDivider()

            // --- تأیید قبل از ثبت دور ---
            SettingsSection(title = stringResource(R.string.settings_round_confirmation_title)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_round_confirmation_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = roundConfirmationEnabled,
                        onCheckedChange = onToggleRoundConfirmation
                    )
                }
            }

            HorizontalDivider()

            // --- حذف تاریخچه ---
            SettingsSection(title = stringResource(R.string.settings_delete_history_title)) {
                OutlinedButton(
                    onClick = { showDeleteHistoryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.btn_delete_history))
                }
            }

            HorizontalDivider()

            // --- اطلاعات برنامه ---
            SettingsSection(title = stringResource(R.string.settings_app_info_title)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_version_label, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDeleteHistoryDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_history_confirm_title),
            confirmText = stringResource(R.string.btn_confirm_undo),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                showDeleteHistoryDialog = false
                onDeleteHistoryConfirmed()
            },
            onDismiss = { showDeleteHistoryDialog = false }
        ) {
            Text(
                text = stringResource(R.string.delete_history_confirm_message),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RenameTeamsForm(
    currentGame: Game,
    onRenameTeams: (String, String) -> Unit
) {
    var teamOneName by remember(currentGame.id) { mutableStateOf(currentGame.teamOneName) }
    var teamTwoName by remember(currentGame.id) { mutableStateOf(currentGame.teamTwoName) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = teamOneName,
            onValueChange = { teamOneName = it },
            label = { Text(stringResource(R.string.team_one_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = teamTwoName,
            onValueChange = { teamTwoName = it },
            label = { Text(stringResource(R.string.team_two_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = { onRenameTeams(teamOneName, teamTwoName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_save_names))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}
