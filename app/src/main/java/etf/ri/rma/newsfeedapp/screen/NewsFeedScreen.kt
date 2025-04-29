package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.data.NewsData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NewsFeedScreen(
    onMoreFiltersClick: () -> Unit,
    onNewsItemClick: (String) -> Unit,
    navController: NavController
) {
    val currentEntry = remember { navController.currentBackStackEntry }
    val filtersState = currentEntry?.savedStateHandle?.getStateFlow("filters", null as Triple<Set<String>, String?, List<String>>?)
    val allNews = remember { NewsData.getAllNews() }
    var selectedCategories by rememberSaveable { mutableStateOf(setOf<String>()) }
    var unwantedWords by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var dateRange by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(filtersState) {
        filtersState?.collect { filters ->
            filters?.let { (categories, dateRangeStr, words) ->
                selectedCategories = categories
                dateRange = dateRangeStr
                unwantedWords = words
            }
        }
    }
    val filteredNews = remember(allNews, selectedCategories, dateRange, unwantedWords) {
        allNews.filter { news ->
            (selectedCategories.isEmpty() || news.category in selectedCategories) &&
                    (dateRange == null || isWithinDateRange(news.publishedDate, dateRange!!)) &&
                    (unwantedWords.isEmpty() || unwantedWords.none { word ->
                        news.title.contains(word, ignoreCase = true) || news.snippet.contains(word, ignoreCase = true)
                    })
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        CategoryFilter(
            selectedCategories = selectedCategories,
            onSelectionChanged = { selectedCategories = it }
        )
        FilterChip(
            selected = false,
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set("initialCategories", selectedCategories)
                onMoreFiltersClick()
            },
            label = { Text("Više filtera ...") },
            modifier = Modifier.testTag("filter_chip_more")
        )
        if (filteredNews.isEmpty()) {
            MessageCard(
                text = if (selectedCategories.isEmpty()) "Nema dostupnih vijesti."
                else "Nema pronađenih vijesti za odabrane kategorije."
            )
        } else {
            NewsList(newsItems = filteredNews, onItemClick = { onNewsItemClick(it.id) })
        }
    }
}


fun isWithinDateRange(date: String, range: String): Boolean {
    val format = DateTimeFormatter.ofPattern("d-MM-yyyy")
    val parsedDate = LocalDate.parse(date, format)
    val (startStr, endStr) = range.split(";")
    val startDate = LocalDate.parse(startStr, format)
    val endDate = LocalDate.parse(endStr, format)
    return parsedDate in startDate..endDate
}
