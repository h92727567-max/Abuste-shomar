package com.abusteh.shomar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.R
import com.abusteh.shomar.domain.model.Game
import com.abusteh.shomar.domain.model.Round
import com.abusteh.shomar.domain.rules.ScoringEngine
import com.abusteh.shomar.domain.rules.Team
import com.abusteh.shomar.ui.components.ConfirmDialog
import com.abusteh.shomar.ui.components.TeamCard
import com.abusteh.shomar.ui.theme.TeamOneColor
import com.abusteh.shomar.ui.theme.TeamOneColorContainer
import com.abusteh.shomar.ui.theme.TeamTwoColor
import com.abusteh.shomar.ui.theme.TeamTwoColorContainer

/**
 * صفحه بازی (بخش ۶ مشخصات).
 *
 * نمایش: نام و امتیاز دو گروه، دور فعلی، خلاصه آخرین دور ثبت‌شده
 * (یا «هنوز ثبت نشده» اگر هیچ دوری ثبت نشده باشد) و گروه پیشتاز.
 *
 * دکمه «لغو آخرین دور» (بخش ۱۵): قبل از حذف واقعی، Dialog تأیید نشان
 * داده می‌شود؛ اگر هنوز هیچ دوری ثبت نشده باشد، این دکمه غیرفعال است.
 *
 * پایان بازی (بخش ۱۶): اگر امتیاز یکی از گروه‌ها به ۶۲ یا بیشتر رسیده
 * باشد، به‌جای صفحه معمولی بازی، صفحه اعلام برنده نشان داده می‌شود.
 */
@Composable
fun GameScreen(
    game: Game,
    rounds: List<Round> = emptyList(),
    onRegisterRoundClick: () -> Unit = {},
    onUndoLastRoundClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onStartNewGameClick: () -> Unit = {}
) {
    val winner = ScoringEngine.winner(game)

    if (winner != null) {
        GameOverContent(
            game = game,
            winner = winner,
            onStartNewGameClick = onStartNewGameClick
        )
        return
    }

    val leadingTeamText = when {
        game.teamOneScore > game.teamTwoScore -> game.teamOneName
        game.teamTwoScore > game.teamOneScore -> game.teamTwoName
        else -> stringResource(R.string.tie_label)
    }
    val teamOneIsLeading = game.teamOneScore > game.teamTwoScore
    val teamTwoIsLeading = game.teamTwoScore > game.teamOneScore

    val lastRound = rounds.maxByOrNull { it.roundNumber }
    val lastRoundText = if (lastRound == null) {
        stringResource(R.string.no_round_yet)
    } else {
        val readerName = if (lastRound.readerTeam == Team.TEAM_ONE) game.teamOneName else game.teamTwoName
        val resultText = if (lastRound.successful) {
            stringResource(R.string.result_success)
        } else {
            stringResource(R.string.result_fail)
        }
        stringResource(R.string.last_round_summary, readerName, lastRound.bid, resultText)
    }

    var showUndoConfirmDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeamCard(
                    teamName = game.teamOneName,
                    score = game.teamOneScore,
                    accentColor = TeamOneColor,
                    accentContainerColor = TeamOneColorContainer,
                    isLeading = teamOneIsLeading,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                TeamCard(
                    teamName = game.teamTwoName,
                    score = game.teamTwoScore,
                    accentColor = TeamTwoColor,
                    accentContainerColor = TeamTwoColorContainer,
                    isLeading = teamTwoIsLeading,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            HorizontalDivider()

            InfoRow(
                label = stringResource(R.string.current_round_label),
                value = game.currentRoundNumber.toString()
            )
            InfoRow(
                label = stringResource(R.string.last_round_label),
                value = lastRoundText
            )
            InfoRow(
                label = stringResource(R.string.leading_team_label),
                value = leadingTeamText,
                highlight = game.teamOneScore != game.teamTwoScore
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRegisterRoundClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_register_round),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showUndoConfirmDialog = true },
                    enabled = rounds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.btn_undo_last_round))
                }

                OutlinedButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.btn_history))
                }
            }
        }
    }

    if (showUndoConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.undo_dialog_title),
            confirmText = stringResource(R.string.btn_confirm_undo),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                showUndoConfirmDialog = false
                onUndoLastRoundClick()
            },
            onDismiss = { showUndoConfirmDialog = false }
        ) {
            Text(
                text = stringResource(R.string.undo_dialog_message),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GameOverContent(
    game: Game,
    winner: Team,
    onStartNewGameClick: () -> Unit
) {
    val winnerName = if (winner == Team.TEAM_ONE) game.teamOneName else game.teamTwoName
    val winnerColor = if (winner == Team.TEAM_ONE) TeamOneColor else TeamTwoColor

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.game_over_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.winner_label),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = winnerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = winnerColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.final_score_label),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${game.teamOneName}: ${game.teamOneScore}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${game.teamTwoName}: ${game.teamTwoScore}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStartNewGameClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_new_game_after_end),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End
        )
    }
}
