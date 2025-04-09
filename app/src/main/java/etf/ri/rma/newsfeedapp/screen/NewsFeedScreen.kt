package etf.ri.rma.newsfeedapp.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.data.NewsData

class NewsFeedScreen {

    @SuppressLint("NotConstructor")
    @Composable
    fun NewsFeedScreen() {
        val allNews = remember { NewsData.getAllNews() }
        var selectedCategory by remember { mutableStateOf<String?>(null) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            CategoryFilter(selectedCategory = selectedCategory, onCategorySelected = { category ->
                selectedCategory = if (selectedCategory == category) null else category
            })

            val filteredItems = allNews.filter {
                selectedCategory == null || it.category == selectedCategory
            }

            val message = if (selectedCategory != null)
                "Nema pronađenih vijesti u kategoriji $selectedCategory."
            else
                "Nema dostupnih vijesti."

            NewsList(newsItems = filteredItems.ifEmpty { emptyList() })
        }
    }


    @Composable
    fun CategoryFilter(selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
        val categories = listOf("Sve", "Politika", "Sport", "Nauka/tehnologija", "Biznis")

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category || (selectedCategory == null && category == "Sve"),
                    onClick = { onCategorySelected(if (category == "Sve") null else category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

