package com.abusteh.shomar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ردیف افقی گزینه‌های تک‌انتخابی (مثلاً انتخاب گروه خواننده، مقدار خواندن، نتیجه یا کوت).
 * کامپوننت خالص UI است و هیچ منطق بازی در آن نیست.
 */
@Composable
fun <T> OptionSelector(
    options: List<T>,
    selected: T?,
    labelFor: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(labelFor(option)) }
            )
        }
    }
}
