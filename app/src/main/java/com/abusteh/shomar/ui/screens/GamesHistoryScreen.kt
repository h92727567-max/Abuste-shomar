package com.abusteh.shomar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.R
import com.abusteh.shomar.domain.model.GameSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * صفحه «تاریخچه بازی‌ها» (بخش ۱۰ مشخصات): لیست بازی‌های تمام‌شده.
 * با انتخاب هر بازی، جزئیات دورهای آن نمایش داده می‌شود (بخش ۱۰ مشخصات).
 *
 * نکته: تاریخ فعلاً با تقویم میلادی (عددی، بدون وابستگی به Locale خاص)
 * نمایش داده می‌شود. تبدیل به تقویم شمسی می‌تواند بعداً در Phase 12
 * (طراحی نهایی UI) اضافه شود.
 */
@Composable
fun GamesHistoryScreen(
    games: List<GameSummary>,
    onGameClick: (GameSummary) -> Unit
) {
    Scaffold { innerPadding ->
        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.games_history_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(games.sortedByDescending { it.endDate ?: it.startDate }) { gameSummary ->
                    GameSummaryCard(
                        gameSummary = gameSummary,
                        onClick = { onGameClick(gameSummary) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameSummaryCard(gameSummary: GameSummary, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) }
    val dateText = dateFormatter.format(Date(gameSummary.endDate ?: gameSummary.startDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.games_history_matchup,
                    gameSummary.teamOneName,
                    gameSummary.teamTwoName
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.games_history_score,
                    gameSummary.teamOneScore,
                    gameSummary.teamTwoScore
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${stringResource(R.string.winner_label)}: ${gameSummary.winnerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.game_date_label)}: $dateText",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
