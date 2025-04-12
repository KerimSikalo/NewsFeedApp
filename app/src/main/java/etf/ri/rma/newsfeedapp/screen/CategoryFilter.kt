package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryFilter(selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
    val mainCategories = listOf(
        "Sve" to "filter_chip_all",
        "Politika" to "filter_chip_pol",
        "Sport" to "filter_chip_spo",
        "Nauka/tehnologija" to "filter_chip_sci"
    )
    val extraCategories = listOf(
        "Biznis" to "filter_chip_none"
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mainCategories.forEach { (category, tag) ->
                val isSelected = selectedCategory == category || (selectedCategory == null && category == "Sve")
                FilterChip(
                    modifier = Modifier.semantics { contentDescription = tag }
                        .testTag(tag)
                        .widthIn(
                            max = if (category == "Nauka/tehnologija") 120.dp
                            else Dp.Unspecified
                        ),
                    selected = isSelected,
                    onClick = { if (!isSelected) onCategorySelected(if (category == "Sve") null else category) },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            extraCategories.forEach { (category, tag) ->
                val isSelected = selectedCategory == category
                FilterChip(
                    modifier = Modifier.semantics { contentDescription = tag }
                        .testTag(tag),
                    selected = isSelected,
                    onClick = { if (!isSelected) onCategorySelected(category) },
                    label = { Text(category) },
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
