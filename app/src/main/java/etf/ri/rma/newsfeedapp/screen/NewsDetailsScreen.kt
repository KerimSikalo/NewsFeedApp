package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsDetailsScreen(
    newsItem: NewsItem,
    onRelatedNewsClick: (String) -> Unit,
    onClose: () -> Unit
) {
    val relatedNews = remember(newsItem) { findRelatedNews(newsItem) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = newsItem.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("details_title")
        )
        Text(
            text = newsItem.snippet,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag("details_snippet")
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Kategorija: ${newsItem.category}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("details_category")
        )
        Text(
            text = "${newsItem.source} • ${newsItem.publishedDate}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.testTag("details_source")
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (relatedNews.isNotEmpty()) {
            Text("Povezane vijesti iz iste kategorije", style = MaterialTheme.typography.titleMedium)
            relatedNews.forEachIndexed { index, related ->
                Text(
                    text = related.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .testTag("related_news_title_${index + 1}")
                        .clickable { onRelatedNewsClick(related.id) }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onClose() },
            modifier = Modifier.testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
        BackHandler {
            onClose()
        }
    }
}


fun findRelatedNews(current: NewsItem): List<NewsItem> {
    return NewsData.getAllNews()
        .filter { it.category == current.category && it.id != current.id }
        .sortedWith(compareBy({ dateDifference(it.publishedDate, current.publishedDate) }, { it.title }))
        .take(2)
}


fun dateDifference(date1: String, date2: String): Int {
    val format = java.time.format.DateTimeFormatter.ofPattern("d-MM-yyyy")
    val d1 = java.time.LocalDate.parse(date1, format)
    val d2 = java.time.LocalDate.parse(date2, format)
    return kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(d1, d2).toInt())
}