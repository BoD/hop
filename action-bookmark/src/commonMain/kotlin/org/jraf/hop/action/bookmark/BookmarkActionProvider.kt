/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jraf.hop.action.bookmark

import kotlinx.io.files.Path
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.ActionProvider.Result
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.actions
import org.jraf.hop.action.bookmark.BookmarkActionProvider.Configuration.Icon
import org.jraf.hop.action.bookmark.util.lastModified
import org.jraf.hop.action.util.openUrl
import org.jraf.hop.action.util.suspendRunCatching
import org.jraf.hop.action_bookmark.generated.resources.Res
import org.jraf.hop.action_bookmark.generated.resources.brave
import org.jraf.klibnanolog.logd
import org.jraf.klibnanolog.logw
import kotlin.time.Instant

class BookmarkActionProvider(
  private val configuration: Configuration,
) : ActionProvider {
  private class CachedBookmarks(
    val bookmarks: List<UrlNode>,
    val fileLastModified: Instant,
  )

  private var cachedBookmarks: CachedBookmarks? = null

  override fun provide(query: String): Result {
    return actions {
      ensureBookmarksLoaded()
      val cachedBookmarks = cachedBookmarks ?: run {
        emit(emptyList())
        return@actions
      }
      val bookmarks = cachedBookmarks.bookmarks
        .filter { it.name.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
      emit(bookmarks.map { urlNode -> BookmarkAction(configuration, urlNode) })
    }
  }

  private suspend fun ensureBookmarksLoaded() {
    if (cachedBookmarks == null) {
      loadBookmarks()
    } else {
      val lastModified = configuration.bookmarksFilePath.lastModified()
      if (lastModified == null) {
        logw("Can't load metadata for bookmarks file ${configuration.bookmarksFilePath}")
        return
      }
      if (lastModified > cachedBookmarks!!.fileLastModified) {
        logd("Bookmarks file ${configuration.bookmarksFilePath} has been modified since last load, reloading bookmarks")
        loadBookmarks()
      }
    }
  }

  private suspend fun loadBookmarks() {
    suspendRunCatching {
      logd("Loading bookmarks from ${configuration.bookmarksFilePath}")
      val bookmarks = loadBookmarks(configuration.bookmarksFilePath)
      val lastModified = configuration.bookmarksFilePath.lastModified()!!
      cachedBookmarks = CachedBookmarks(bookmarks, lastModified)
    }.onFailure {
      logw(it, "Failed to load bookmarks from ${configuration.bookmarksFilePath}")
    }
  }

  data class Configuration(
    val pathToBrowser: String,
    val icon: Icon,
  ) {
    sealed interface Icon {
      class Bundled {
        object Brave : Icon
      }

      class Url(val url: String) : Icon
    }
  }

  private val Configuration.bookmarksFilePath get() = Path(pathToBrowser, "Default", "Bookmarks")
}

private data class BookmarkAction(
  private val configuration: BookmarkActionProvider.Configuration,
  private val urlNode: UrlNode,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:$urlNode"
  override val primaryText: String = urlNode.name
  override val secondaryText: String = urlNode.url
  override val icon = when (configuration.icon) {
    Icon.Bundled.Brave -> Action.Icon.ResourceIcon(Res.drawable.brave)
    is Icon.Url -> Action.Icon.UriIcon(configuration.icon.url)
  }

  override suspend fun execute() {
    openUrl(urlNode.url)
  }
}
