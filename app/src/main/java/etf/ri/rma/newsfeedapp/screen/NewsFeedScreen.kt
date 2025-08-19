package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.data.network.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.isConnected
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
    val context = LocalContext.current
    val categoryMap = mapOf(
        "Politika" to "politics",
        "Sport" to "sports",
        "Nauka/tehnologija" to "science",
        "Biznis" to "business",
        "Zdravlje" to "health",
        "Hrana" to "food",
        "Putovanja" to "travel",
        "Zabava" to "entertainment"
    )

    val currentEntry = navController.currentBackStackEntryAsState().value
    val filtersState = currentEntry?.savedStateHandle?.getStateFlow("filters", null as Triple<Set<String>, String?, List<String>>?)
    val initialCategories = currentEntry?.savedStateHandle?.get<Set<String>>("initialCategories") ?: setOf()
    var selectedCategories by remember { mutableStateOf(initialCategories) }
    var allNews by remember { mutableStateOf(emptyList<NewsItem>()) }
    var unwantedWords by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var dateRange by rememberSaveable { mutableStateOf<String?>(null) }
    var filtersApplied by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var isFilterVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentScrollOffset) ->
            val isScrollingDown = if (currentIndex > previousIndex) {
                true
            } else if (currentIndex < previousIndex) {
                false
            } else {
                currentScrollOffset > previousScrollOffset
            }

            if (currentIndex == 0 && currentScrollOffset < 100) {
                isFilterVisible = true
            } else if (isScrollingDown && currentIndex > 0) {
                isFilterVisible = false
            } else if (!isScrollingDown) {
                isFilterVisible = true
            }
            previousIndex = currentIndex
            previousScrollOffset = currentScrollOffset
        }
    }

    LaunchedEffect(Unit) {
        try {
            RetrofitInstance.defaultNewsDAO.loadInitialNews(context)
            allNews = RetrofitInstance.defaultNewsDAO.getAllStories()
        } catch (_: Exception) {}
    }

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
        val dao = RetrofitInstance.defaultNewsDAO
        val savedDao = NewsDatabase.getDatabase(context).savedNewsDAO()
        if (!isConnected(context)) {
            allNews = if (selectedCategories.isEmpty()) {
                savedDao.allNews().sortedByDescending { it.isFeatured }
            } else {
                selectedCategories.flatMap { displayCategory ->
                    val apiCategory = categoryMap[displayCategory]
                    return@flatMap if (apiCategory != null) {
                        try {
                            savedDao.getNewsWithCategory(apiCategory)
                        } catch (e: Exception) {
                            Log.e("OfflineLoad", "Greška: ${e.message}")
                            emptyList()
                        }
                    } else emptyList()
                }.sortedByDescending { it.isFeatured }
            }
        } else {
            if (selectedCategories.isEmpty()) {
                try {
                    dao.getTopStoriesByCategory("general", context, RetrofitInstance.defaultImagaDAO)
                } catch (_: Exception) {}
                allNews = dao.getAllStories().sortedByDescending { it.isFeatured }
            } else if (selectedCategories.size == 1) {
                val displayCategory = selectedCategories.first()
                val apiCategory = categoryMap[displayCategory]
                if (apiCategory != null) {
                    try {
                        dao.getTopStoriesByCategory(apiCategory, context, RetrofitInstance.defaultImagaDAO)
                    } catch (_: Exception) {}
                    allNews = dao.getAllStories().filter { it.category == apiCategory }.sortedByDescending { it.isFeatured }
                } else {
                    allNews = emptyList()
                }
            } else {
                allNews = dao.getAllStories()
            }
        }
    }

    val filteredNews = remember(allNews, selectedCategories, dateRange, unwantedWords) {
        val mappedSelected = selectedCategories.mapNotNull { categoryMap[it] }.toSet()
        allNews
            .distinctBy { it.uuid }.filter { news ->
                (mappedSelected.isEmpty() || news.category in mappedSelected) &&
                        (dateRange == null || isWithinDateRange(news.publishedDate, dateRange!!)) &&
                        (unwantedWords.isEmpty() || unwantedWords.none { word ->
                            news.title.contains(word, ignoreCase = true) || news.snippet.contains(word, ignoreCase = true)
                        })
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "News Feed",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    FilterChip(
                        selected = filtersApplied || unwantedWords.isNotEmpty() || dateRange != null,
                        onClick = {
                            val selected = if (selectedCategories.isEmpty()) "_" else selectedCategories.joinToString(",")
                            navController.navigate("filter/$selected")
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Više filtera",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .testTag("filter_chip_more")
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                CategoryFilter(
                    selectedCategories = selectedCategories,
                    onSelectionChanged = {
                        selectedCategories = it
                        navController.currentBackStackEntry?.savedStateHandle?.set("filters", Triple(selectedCategories, dateRange, unwantedWords))
                        navController.currentBackStackEntry?.savedStateHandle?.set("initialCategories", selectedCategories)
                    },
                    isVisible = isFilterVisible
                )
            }
        }

        if (filteredNews.isEmpty()) {
            MessageCard(
                text = if (selectedCategories.isEmpty()) "Nema dostupnih vijesti."
                else "Nema pronađenih vijesti za odabrane kategorije."
            )
        } else {
            NewsListWithScroll(
                newsItems = filteredNews,
                onItemClick = { onNewsItemClick(it.uuid) },
                listState = listState
            )
        }
    }
}

@Composable
fun NewsListWithScroll(
    newsItems: List<NewsItem>,
    onItemClick: (NewsItem) -> Unit,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(newsItems, key = { index, item -> "${item.uuid}-${item.publishedDate}-$index" }) { _, newsItem ->
            val cardModifier = Modifier.fillMaxWidth().clickable { onItemClick(newsItem) }
            if (newsItem.isFeatured) {
                FeaturedNewsCard(newsItem = newsItem, modifier = cardModifier)
            } else {
                StandardNewsCard(newsItem = newsItem, modifier = cardModifier)
            }
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