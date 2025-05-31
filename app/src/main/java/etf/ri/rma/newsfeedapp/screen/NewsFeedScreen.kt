package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import etf.ri.rma.newsfeedapp.data.network.RetrofitInstance
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NewsFeedScreen(
    onMoreFiltersClick: () -> Unit,
    onNewsItemClick: (String) -> Unit,
    navController: NavController
) {
    val categoryMap = mapOf(
        "Politika" to "politics",
        "Sport" to "sports",
        "Nauka/tehnologija" to "science",
        "Biznis" to "business"
    )
    val currentEntry = navController.currentBackStackEntryAsState().value
    val filtersState = currentEntry?.savedStateHandle?.getStateFlow("filters", null as Triple<Set<String>, String?, List<String>>?)
    val initialCategories = currentEntry?.savedStateHandle?.get<Set<String>>("initialCategories") ?: setOf()
    var selectedCategories by remember { mutableStateOf(initialCategories) }
    val currentCategoriesState = remember { mutableStateOf<Set<String>>(initialCategories) }
    var allNews by remember { mutableStateOf(emptyList<NewsItem>()) }
    LaunchedEffect(Unit) {
        try {
            RetrofitInstance.defaultNewsDAO.loadInitialNews()
            allNews = RetrofitInstance.defaultNewsDAO.getAllStories()
        } catch (_: Exception) {}
    }
    var unwantedWords by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var dateRange by rememberSaveable { mutableStateOf<String?>(null) }
    var filtersApplied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(currentEntry) {
        filtersState?.collect { filters ->
            filters?.let { (categories, dateRangeStr, words) ->
                selectedCategories = categories
                dateRange = dateRangeStr
                unwantedWords = words
                filtersApplied = true
            }
        }
    }
    LaunchedEffect(selectedCategories) {
        if (selectedCategories.isEmpty()) {
            val categories = categoryMap.values
            categories.forEach { category ->
                try {
                    RetrofitInstance.defaultNewsDAO.getTopStoriesByCategory(category)
                } catch (_: Exception) {}
            }
            allNews = RetrofitInstance.defaultNewsDAO.getAllStories()
        } else if (selectedCategories.size == 1) {
            val displayCategory = selectedCategories.first()
            val apiCategory = categoryMap[displayCategory]
            if (apiCategory != null) {
                try {
                    RetrofitInstance.defaultNewsDAO.getTopStoriesByCategory(apiCategory)
                } catch (_: Exception) {}
                allNews = RetrofitInstance.defaultNewsDAO.getAllStories()
            }
        }
    }
    val filteredNews = remember(allNews, selectedCategories, dateRange, unwantedWords) {
        val mappedSelected = selectedCategories.mapNotNull { categoryMap[it] }.toSet()
        allNews.filter { news ->
            (mappedSelected.isEmpty() || news.category in mappedSelected) &&
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
            onSelectionChanged = {
                selectedCategories = it
                currentCategoriesState.value = it
                navController.currentBackStackEntry?.savedStateHandle?.set("filters", Triple(selectedCategories, dateRange, unwantedWords))
                navController.currentBackStackEntry?.savedStateHandle?.set("initialCategories", selectedCategories)
            }
        )
        FilterChip(
            selected = selectedCategories.isNotEmpty(),
            onClick = {
                val selected = if (selectedCategories.isEmpty()) "_" else selectedCategories.joinToString(",")
                navController.navigate("filter/$selected")
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
            NewsList(newsItems = filteredNews, onItemClick = { onNewsItemClick(it.uuid) })
        }
    }
}

fun formatDateToDdMmYyyy(dateStr: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("d-MM-yyyy")
    val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val date = LocalDate.parse(dateStr, inputFormatter)
    return outputFormatter.format(date)
}

fun isWithinDateRange(date: String, range: String): Boolean {
    val format = DateTimeFormatter.ofPattern("d-MM-yyyy", Locale.ENGLISH)
    val parsedDate = LocalDate.parse(formatDateToDdMmYyyy(date), format)
    val (startStr, endStr) = range.split(";")
    val startDate = LocalDate.parse(startStr, format)
    val endDate = LocalDate.parse(endStr, format)
    return parsedDate in startDate..endDate
}








