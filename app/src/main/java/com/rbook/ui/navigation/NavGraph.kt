package com.rbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rbook.ui.screens.bookshelf.BookshelfScreen
import com.rbook.ui.screens.reader.ReaderScreen
import com.rbook.ui.screens.stats.StatsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "bookshelf"
    ) {
        composable("bookshelf") {
            BookshelfScreen(
                onNavigateToReader = { bookId ->
                    navController.navigate("reader/$bookId")
                },
                onNavigateToStats = {
                    navController.navigate("stats")
                }
            )
        }
        
        composable("reader/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull()
            if (bookId != null) {
                ReaderScreen(
                    bookId = bookId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        composable("stats") {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
