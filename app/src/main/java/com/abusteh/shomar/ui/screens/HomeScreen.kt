package com.abusteh.shomar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.R
import com.abusteh.shomar.ui.components.TeamCard
import com.abusteh.shomar.ui.theme.TeamOneColor
import com.abusteh.shomar.ui.theme.TeamOneColorContainer
import com.abusteh.shomar.ui.theme.TeamTwoColor
import com.abusteh.shomar.ui.theme.TeamTwoColorContainer

/**
 * صفحه اصلی برنامه.
 *
 * نکته Phase 1: این صفحه فقط UI اولیه است. منطق واقعی دکمه‌ها
 * (شروع بازی، ادامه بازی، تاریخچه، تنظیمات) و ناوبری بین صفحات
 * در فازهای بعدی (Navigation در Phase 2 به بعد) اضافه می‌شود.
 * وجود بازی ذخیره‌شده هم فعلاً به‌صورت ثابت false در نظر گرفته شده
 * تا دکمه «ادامه بازی» طبق مشخصات، غیرفعال نمایش داده شود.
 */
@Composable
fun HomeScreen(
    hasSavedGame: Boolean = false,
    onNewGameClick: () -> Unit = {},
    onContinueGameClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TeamCard(
                teamName = stringResource(R.string.team_default_1),
                score = 0,
                accentColor = TeamOneColor,
                accentContainerColor = TeamOneColorContainer
            )

            TeamCard(
                teamName = stringResource(R.string.team_default_2),
                score = 0,
                accentColor = TeamTwoColor,
                accentContainerColor = TeamTwoColorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNewGameClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_new_game),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onContinueGameClick,
                enabled = hasSavedGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_continue_game),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onHistoryClick,
                colors = ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_history),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_settings),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
