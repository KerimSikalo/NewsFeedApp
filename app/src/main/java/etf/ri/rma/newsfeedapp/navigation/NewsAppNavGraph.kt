package etf.ri.rma.newsfeedapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.data.network.RetrofitInstance
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen

@Composable
fun NewsAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            NewsFeedScreen(
                onMoreFiltersClick = { navController.navigate("filters") },
                onNewsItemClick = { uuid -> navController.navigate("details/$uuid") },
                navController = navController
            )
        }
        composable(
            route = "filter/{selectedCategories}",
            arguments = listOf(
                navArgument("selectedCategories") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val selectedCategoriesArg = backStackEntry.arguments?.getString("selectedCategories") ?: ""
            val selectedCategories =
                if (selectedCategoriesArg.isNotBlank() && selectedCategoriesArg != "_") selectedCategoriesArg.split(",").toSet()
                else emptySet()
            FilterScreen(
                initialCategories = selectedCategories,
                onApplyFilters = { categories, dateRange, unwantedWords ->
                    backStackEntry.savedStateHandle.set("filters", Triple(categories, dateRange, unwantedWords))
                },
                navController = navController
            )
        }
        composable("details/{uuid}") { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid")
            val item = RetrofitInstance.defaultNewsDAO.getAllStories().find { it.uuid == uuid }
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







