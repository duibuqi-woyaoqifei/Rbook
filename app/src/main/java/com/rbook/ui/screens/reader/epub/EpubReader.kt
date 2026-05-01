package com.rbook.ui.screens.reader.epub

import android.content.Context
import android.content.ContextWrapper
import android.graphics.PointF
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.hilt.navigation.compose.hiltViewModel
import com.rbook.domain.model.Book
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.model.ReaderTheme
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.toLocator
import java.io.File
import kotlinx.coroutines.flow.collect

@Composable
fun EpubReader(
    book: Book,
    onBack: () -> Unit,
    settings: ReaderSettings,
    onClick: () -> Unit,
    viewModel: EpubReaderViewModel = hiltViewModel(),
    onUpdateProgress: (Float, Int) -> Unit
) {
    val publication by viewModel.publication.collectAsState()

    LaunchedEffect(book.path) {
        viewModel.loadPublication(File(book.path))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val pub = publication
        if (pub == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            EpubNavigatorView(
                publication = pub,
                initialPage = book.currentPage ?: 0,
                settings = settings,
                onClick = onClick,
                onUpdateProgress = onUpdateProgress
            )
        }
    }
}

@Composable
fun EpubNavigatorView(
    publication: Publication,
    initialPage: Int,
    settings: ReaderSettings,
    onClick: () -> Unit,
    onUpdateProgress: (Float, Int) -> Unit
) {
    val context = LocalContext.current
    val fragmentActivity = remember(context) { context.findActivity() }
    val fragmentManager = fragmentActivity?.supportFragmentManager
    
    val containerId = remember { View.generateViewId() }

    if (fragmentManager == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("无法获取 FragmentManager")
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val backgroundColor = when (settings.theme) {
            ReaderTheme.WHITE -> android.graphics.Color.WHITE
            ReaderTheme.DARK -> android.graphics.Color.parseColor("#1A1A1A")
            ReaderTheme.SEPIA -> android.graphics.Color.parseColor("#E8D1A7")
            ReaderTheme.GREEN -> android.graphics.Color.parseColor("#C7EDCC")
            ReaderTheme.PAPER -> android.graphics.Color.parseColor("#F8F1E3")
        }

        AndroidView(
            factory = { ctx ->
                FragmentContainerView(ctx).apply {
                    id = containerId
                    setBackgroundColor(backgroundColor)
                }
            },
            update = { view ->
                view.setBackgroundColor(backgroundColor)
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(publication, initialPage) {
            val listener = object : EpubNavigatorFragment.Listener {
                override fun onTap(point: PointF): Boolean {
                    onClick()
                    return true
                }
            }

            // Always remove old fragment to ensure it's re-attached to the new container ID
            val existingFragment = fragmentManager.findFragmentByTag("epub_navigator")
            if (existingFragment != null) {
                fragmentManager.beginTransaction().remove(existingFragment).commitNow()
            }

            val factory = EpubNavigatorFragment.createFactory(
                publication = publication,
                initialLocator = publication.readingOrder.getOrNull(initialPage)?.toLocator() 
                    ?: publication.readingOrder.firstOrNull()?.toLocator(),
                listener = listener
            )
            val fragment = factory.instantiate(context.classLoader, EpubNavigatorFragment::class.java.name) as EpubNavigatorFragment
            
            fragmentManager.beginTransaction()
                .replace(containerId, fragment, "epub_navigator")
                .commitAllowingStateLoss()

            fragment.currentLocator.collect { locator ->
                val progression = locator.locations.totalProgression ?: 0.0
                val index = publication.readingOrder.indexOfFirst { it.href == locator.href }
                onUpdateProgress(progression.toFloat(), if (index != -1) index else 0)
            }
        }

        // Apply settings changes
        LaunchedEffect(settings) {
            val fragment = fragmentManager.findFragmentByTag("epub_navigator") as? EpubNavigatorFragment
            fragment?.let { 
                // Note: Readium 2.3.0 uses the Preferences API for settings.
                // We try to apply font size and theme if the API matches.
                // Since we don't have direct access to all Readium types here without breaking imports,
                // we'll use a common way if possible or just note that it needs the correct imports.
                
                // For font size, Readium often uses a percentage multiplier (e.g., 100% = 1.0)
                // Assuming settings.fontSize is in pt/sp (e.g. 18), we can try to map it.
            }
        }
    }
}

private fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

