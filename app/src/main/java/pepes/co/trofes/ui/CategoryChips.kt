package pepes.co.trofes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Simple selectable chips row (scrollable).
 *
 * - [categories] list of labels.
 * - [selected] currently selected label.
 * - [onSelected] callback when user selects a chip.
 */
@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(categories, key = { it }) { label ->
            val isSelected = label == selected

            FilterChip(
                selected = isSelected,
                onClick = { onSelected(label) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else Color(0xFF1F1F1F),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2E7D32),
                    containerColor = Color(0xFFF3F3F3),
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 0.dp,
                ),
            )
        }
    }
}
