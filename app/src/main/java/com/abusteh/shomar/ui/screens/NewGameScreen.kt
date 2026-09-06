package com.abusteh.shomar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.R
import com.abusteh.shomar.viewmodel.GameViewModel

/**
 * صفحه شروع بازی جدید (بخش ۵ مشخصات).
 * فقط دریافت نام دو گروه؛ در صورت خالی بودن، نام‌های پیش‌فرض استفاده می‌شوند.
 */
@Composable
fun NewGameScreen(
    onGameStarted: (teamOneName: String, teamTwoName: String) -> Unit
) {
    var teamOneName by remember { mutableStateOf("") }
    var teamTwoName by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.new_game_title),
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = teamOneName,
                onValueChange = { teamOneName = it },
                label = { Text(stringResource(R.string.team_one_name_label)) },
                placeholder = { Text(GameViewModel.DEFAULT_TEAM_ONE_NAME) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = teamTwoName,
                onValueChange = { teamTwoName = it },
                label = { Text(stringResource(R.string.team_two_name_label)) },
                placeholder = { Text(GameViewModel.DEFAULT_TEAM_TWO_NAME) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onGameStarted(teamOneName, teamTwoName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_confirm_start),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
