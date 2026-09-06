package com.abusteh.shomar.ui.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abusteh.shomar.R
import com.abusteh.shomar.domain.model.Round
import com.abusteh.shomar.domain.rules.Team

/**
 * صفحه تاریخچه دورهای یک بازی (بخش ۱۴ مشخصات).
 * تاریخچه به ترتیب دور (از دور اول به آخر) نمایش داده می‌شود.
 *
 * این صفحه در دو جا استفاده می‌شود:
 * - تاریخچه دورهای بازی *جاری* (از صفحه بازی)
 * - جزئیات دورهای یک بازی *قبلی و تمام‌شده* (بخش ۱۰ مشخصات، Phase 10)
 * برای همین به‌جای مدل زنده [com.abusteh.shomar.domain.model.Game]، فقط
 * نام دو گروه را می‌گیرد.
 */
@Composable
fun HistoryScreen(
    teamOneName: String,
    teamTwoName: String,
    rounds: List<Round>
) {
    Scaffold { innerPadding ->
        if (rounds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
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
                items(rounds.sortedBy { it.roundNumber }) { round ->
                    RoundHistoryCard(
                        teamOneName = teamOneName,
                        teamTwoName = teamTwoName,
                        round = round
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundHistoryCard(teamOneName: String, teamTwoName: String, round: Round) {
    val readerTeamName = if (round.readerTeam == Team.TEAM_ONE) teamOneName else teamTwoName
    val resultText = if (round.successful) {
        stringResource(R.string.result_success)
    } else {
        stringResource(R.string.result_fail)
    }
    val kotText = if (round.kot) stringResource(R.string.kot_yes) else stringResource(R.string.kot_no)
    val amountLabel = if (round.successful) {
        stringResource(R.string.confirm_score_label)
    } else {
        stringResource(R.string.confirm_penalty_label)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.history_round_number, round.roundNumber),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HistoryLine(stringResource(R.string.confirm_reader_team_label), readerTeamName)
            HistoryLine(stringResource(R.string.confirm_bid_label), round.bid.toString())
            HistoryLine(stringResource(R.string.confirm_result_label), resultText)
            HistoryLine(stringResource(R.string.confirm_kot_label), kotText)
            HistoryLine(
                amountLabel,
                stringResource(
                    R.string.confirm_amount_for_team,
                    round.appliedAmount,
                    readerAmountTeamName(teamOneName, teamTwoName, round)
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            HistoryLine(
                stringResource(R.string.history_team_one_before_after, teamOneName),
                "${round.teamOneScoreBefore} ← ${round.teamOneScoreAfter}"
            )
            HistoryLine(
                stringResource(R.string.history_team_two_before_after, teamTwoName),
                "${round.teamTwoScoreBefore} ← ${round.teamTwoScoreAfter}"
            )
        }
    }
}

/** نام گروهی که مقدار appliedAmount (امتیاز یا جریمه) واقعاً به آن تعلق گرفته است. */
private fun readerAmountTeamName(teamOneName: String, teamTwoName: String, round: Round): String {
    val amountBelongsToReader = round.successful
    val team = if (amountBelongsToReader) round.readerTeam else opponentOf(round.readerTeam)
    return if (team == Team.TEAM_ONE) teamOneName else teamTwoName
}

private fun opponentOf(team: Team): Team =
    if (team == Team.TEAM_ONE) Team.TEAM_TWO else Team.TEAM_ONE

@Composable
private fun HistoryLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium
    )
}
