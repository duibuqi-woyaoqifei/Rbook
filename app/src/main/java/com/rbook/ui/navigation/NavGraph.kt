package com.rbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rbook.ui.screens.bookshelf.BookshelfScreen
import com.rbook.ui.screens.reader.ReaderScreen

sealed class Screen(val route: String) {
    object Bookshelf : Screen("bookshelf")
    object Stats : Screen("stats")
    object Reader : Screen("reader/{bookId}") {
        fun createRoute(bookId: Long) = "reader/$bookId"
    }
}

@Composable
fun RBookNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Bookshelf.route
    ) {
        composable(Screen.Bookshelf.route) {
            BookshelfScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.Reader.createRoute(bookId))
                },
                onStatsClick = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }
        composable(Screen.Stats.route) {
            com.rbook.ui.screens.stats.StatsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Reader.route) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: -1L
            ReaderScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
