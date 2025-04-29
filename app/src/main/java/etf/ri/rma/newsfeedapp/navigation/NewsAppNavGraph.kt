package etf.ri.rma.newsfeedapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.screen.*
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            NewsFeedScreen(
                onMoreFiltersClick = { navController.navigate("filters") },
                onNewsItemClick = { id -> navController.navigate("details/$id") },
                navController = navController
            )
        }
        composable("filters") {
            FilterScreen(
                initialCategories = emptySet(),
                onApplyFilters = { categories, dateRange, unwantedWords ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("filters", Triple(categories, dateRange, unwantedWords))
                    navController.popBackStack()
                },
                navController = navController
            )
        }
        composable("details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val item = NewsData.getAllNews().find { it.id == id }
            if (item != null) {
                NewsDetailsScreen(
                    newsItem = item,
                    onRelatedNewsClick = { relatedId -> navController.navigate("details/$relatedId") },
                    onClose = { navController.popBackStack("home", inclusive = false) }
                )
            }
        }
    }
}