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
import androidx.compose.material3.MaterialTheme
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
import com.abusteh.shomar.domain.model.Game
import com.abusteh.shomar.domain.rules.AbusteRules
import com.abusteh.shomar.domain.rules.Team
import com.abusteh.shomar.ui.components.ConfirmDialog
import com.abusteh.shomar.ui.components.OptionSelector

/**
 * صفحه ثبت دور جدید (بخش ۷، ۱۱ و ۲۲ مشخصات).
 *
 * جلوگیری از ثبت ناقص (بخش ۲۲): دکمه «ثبت دور» تا وقتی گروه خواننده،
 * مقدار خواندن و نتیجه انتخاب نشده باشند غیرفعال است.
 *
 * تأیید قبل از ثبت (بخش ۱۱): اگر [confirmBeforeRegister] فعال باشد (پیش‌فرض)،
 * پیش از ثبت نهایی، Dialog خلاصه دور را نشان می‌دهد. این مقدار از تنظیمات
 * برنامه (Phase 11) خوانده می‌شود؛ اگر کاربر آن را غیرفعال کرده باشد، با زدن
 * «ثبت دور» بلافاصله (بدون Dialog) دور ثبت می‌شود.
 */
@Composable
fun RoundScreen(
    game: Game,
    confirmBeforeRegister: Boolean = true,
    onConfirmRound: (readerTeam: Team, bid: Int, successful: Boolean, kot: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var readerTeam by remember { mutableStateOf<Team?>(null) }
    var bid by remember { mutableStateOf<Int?>(null) }
    var successful by remember { mutableStateOf<Boolean?>(null) }
    var kot by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isFormValid = readerTeam != null && bid != null && successful != null

    fun readerTeamName(team: Team) = if (team == Team.TEAM_ONE) game.teamOneName else game.teamTwoName

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.round_screen_title),
                style = MaterialTheme.typography.headlineMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.select_reader_team_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                OptionSelector(
                    options = listOf(Team.TEAM_ONE, Team.TEAM_TWO),
                    selected = readerTeam,
                    labelFor = { readerTeamName(it) },
                    onSelect = { readerTeam = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.select_bid_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                OptionSelector(
                    options = (AbusteRules.MIN_BID..AbusteRules.MAX_BID).toList(),
                    selected = bid,
                    labelFor = { it.toString() },
                    onSelect = { bid = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.select_result_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                OptionSelector(
                    options = listOf(true, false),
                    selected = successful,
                    labelFor = {
                        if (it) stringResource(R.string.result_success)
                        else stringResource(R.string.result_fail)
                    },
                    onSelect = { successful = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.select_kot_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                OptionSelector(
                    options = listOf(false, true),
                    selected = kot,
                    labelFor = {
                        if (it) stringResource(R.string.kot_yes)
                        else stringResource(R.string.kot_no)
                    },
                    onSelect = { kot = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val team = readerTeam
                    val bidValue = bid
                    val result = successful
                    if (team == null || bidValue == null || result == null) return@Button

                    if (confirmBeforeRegister) {
                        showConfirmDialog = true
                    } else {
                        onConfirmRound(team, bidValue, result, kot)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_submit_round),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (showConfirmDialog && readerTeam != null && bid != null && successful != null) {
        val currentReaderTeam = readerTeam!!
        val currentBid = bid!!
        val currentSuccessful = successful!!
        val outcome = AbusteRules.calculateRoundOutcome(
            bid = currentBid,
            successful = currentSuccessful,
            kot = kot
        )
        val penaltyOrScoreTeamName = if (currentSuccessful) {
            readerTeamName(currentReaderTeam)
        } else {
            readerTeamName(if (currentReaderTeam == Team.TEAM_ONE) Team.TEAM_TWO else Team.TEAM_ONE)
        }
        val appliedAmount = if (currentSuccessful) outcome.readerScoreDelta else outcome.opponentScoreDelta

        ConfirmDialog(
            title = stringResource(R.string.confirm_round_title),
            confirmText = stringResource(R.string.btn_confirm_register),
            dismissText = stringResource(R.string.btn_back),
            onConfirm = {
                showConfirmDialog = false
                onConfirmRound(currentReaderTeam, currentBid, currentSuccessful, kot)
            },
            onDismiss = { showConfirmDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ConfirmRow(stringResource(R.string.confirm_reader_team_label), readerTeamName(currentReaderTeam))
                ConfirmRow(stringResource(R.string.confirm_bid_label), currentBid.toString())
                ConfirmRow(
                    stringResource(R.string.confirm_result_label),
                    if (currentSuccessful) stringResource(R.string.result_success) else stringResource(R.string.result_fail)
                )
                ConfirmRow(
                    stringResource(R.string.confirm_kot_label),
                    if (kot) stringResource(R.string.kot_yes) else stringResource(R.string.kot_no)
                )
                ConfirmRow(
                    if (currentSuccessful) stringResource(R.string.confirm_score_label) else stringResource(R.string.confirm_penalty_label),
                    stringResource(R.string.confirm_amount_for_team, appliedAmount, penaltyOrScoreTeamName)
                )
            }
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
}
