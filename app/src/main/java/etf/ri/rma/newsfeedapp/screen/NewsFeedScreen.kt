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
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        CategoryFilter(
            selectedCategory = selectedCategory
        ) { selectedCategory = it }
        val filteredNews = allNews.filter { selectedCategory == null || it.category == selectedCategory }
        if (filteredNews.isEmpty()) {
            MessageCard(
                text = if (selectedCategory == null) "Nema dostupnih vijesti."
                else "Nema pronađenih vijesti u kategoriji $selectedCategory."
            )
        } else NewsList(newsItems = filteredNews)
    }
}