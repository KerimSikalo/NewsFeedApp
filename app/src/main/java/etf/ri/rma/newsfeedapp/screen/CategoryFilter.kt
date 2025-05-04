package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryFilter(
    selectedCategories: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    val chipMap = mapOf(
        "Sve" to "filter_chip_all",
        "Politika" to "filter_chip_pol",
        "Sport" to "filter_chip_spo",
        "Nauka/tehnologija" to "filter_chip_sci",
        "Biznis" to "filter_chip_none"
    )
    val isAllSelected = selectedCategories.isEmpty()
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chipMap.forEach { (category, tag) ->
                val isSelected =
                    if (category == "Sve") isAllSelected
                    else selectedCategories.contains(category)
                FilterChip(
                    modifier = Modifier.testTag(tag).widthIn(max = if (category == "Nauka/tehnologija") 120.dp else Dp.Unspecified),
                    selected = isSelected,
                    onClick = {
                        if (category == "Sve") onSelectionChanged(emptySet())
                        else onSelectionChanged(setOf(category))
                    },
                    label = {
                        Text(
                            text = category,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = if (category == "Nauka/tehnologija") 12.sp else 14.sp
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }
    }
}






