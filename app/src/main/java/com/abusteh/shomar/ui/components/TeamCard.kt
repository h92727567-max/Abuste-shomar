package com.abusteh.shomar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * کارت نمایش نام و امتیاز یک گروه.
 * این کامپوننت فقط UI است و هیچ منطق بازی در آن وجود ندارد.
 *
 * وقتی [isLeading] فعال باشد (بخش ۲۴ مشخصات: کاربر باید در یک نگاه بفهمد
 * چه گروهی جلو است)، کارت با یک حاشیه ملایم و نشان 🏆 کنار نام گروه
 * برجسته می‌شود.
 */
@Composable
fun TeamCard(
    teamName: String,
    score: Int,
    accentColor: Color,
    accentContainerColor: Color,
    isLeading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLeading) 4.dp else 1.dp),
        border = if (isLeading) BorderStroke(2.dp, accentColor) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLeading) {
                    Text(text = "🏆", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
