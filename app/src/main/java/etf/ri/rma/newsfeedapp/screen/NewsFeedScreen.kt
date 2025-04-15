package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsFeedScreen() {
    val allNews = remember { NewsData.getAllNews() }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        CategoryFilter(
            selectedCategories = selectedCategories,
            onSelectionChanged = { selectedCategories = it }
        )

        val filteredNews = if (selectedCategories.isEmpty()) {
            allNews
        } else {
            allNews.filter { it.category in selectedCategories }
        }

        if (filteredNews.isEmpty()) {
            MessageCard(
                text = if (selectedCategories.isEmpty()) "Nema dostupnih vijesti."
                else "Nema pronađenih vijesti za odabrane kategorije."
            )
        } else {
            NewsList(newsItems = filteredNews)
        }
    }
}
