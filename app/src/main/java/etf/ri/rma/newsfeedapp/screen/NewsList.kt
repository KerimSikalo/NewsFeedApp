package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsList(newsItems: List<NewsItem>, onItemClick: (NewsItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.testTag("news_list").padding(8.dp)
    ) {
        items(newsItems, key = { it.id }) { item ->
            val cardModifier = Modifier.fillMaxWidth().clickable { onItemClick(item) }
            if (item.isFeatured) FeaturedNewsCard(newsItem = item, modifier = cardModifier)
            else StandardNewsCard(newsItem = item, modifier = cardModifier)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}







