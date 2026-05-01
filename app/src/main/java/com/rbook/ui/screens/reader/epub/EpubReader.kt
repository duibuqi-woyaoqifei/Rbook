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
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.toLocator
import java.io.File

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
