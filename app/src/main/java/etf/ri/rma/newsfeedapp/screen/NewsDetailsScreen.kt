package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.data.network.RetrofitInstance
import etf.ri.rma.newsfeedapp.model.NewsItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsDetailsScreen(
    newsItem: NewsItem,
    onRelatedNewsClick: (String) -> Unit,
    onClose: () -> Unit
) {
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var relatedNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    LaunchedEffect(newsItem.imageUrl) {
        newsItem.imageUrl?.let {
            try {
                tags = RetrofitInstance.defaultImagaDAO.getTags(it)
            } catch (_: Exception) {}
        }
    }
    LaunchedEffect(newsItem) {
        try {
            relatedNews = RetrofitInstance.defaultNewsDAO.getSimilarStories(newsItem.uuid)
        } catch (_: Exception) {
            relatedNews = emptyList()
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            newsItem.imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("details_title")
            )
            Spacer(modifier = Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (tags.isNotEmpty()) {
            item {
                Text("Tagovi slike:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach {
                        Text(
                            text = "#$it",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (relatedNews.isNotEmpty()) {
            item {
                Text("Povezane vijesti iz iste kategorije", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(relatedNews, key = { it.uuid }) { related ->
                Text(
                    text = related.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRelatedNewsClick(related.uuid) }
                        .padding(vertical = 4.dp)
                        .testTag("related_news_title_${related.uuid.take(4)}")
                )
            }

        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onClose() },
                modifier = Modifier.fillMaxWidth().testTag("details_close_button")
            ) {
                Text("Zatvori detalje")
            }
        }
    }
    BackHandler {
        onClose()
    }
}