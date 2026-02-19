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

package org.jraf.hop.action.websearch

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import hop.action_websearch.generated.resources.Res
import hop.action_websearch.generated.resources.google
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.websearch.WebSearchActionProvider.Configuration.Icon
import org.jraf.hop.action.websearch.util.openUrl
import org.jraf.hop.action.websearch.util.urlEncoded
import org.jraf.klibnanolog.logw

class WebSearchActionProvider(
  private val configuration: Configuration,
) : ActionProvider {
  private class CachedIcon(
    val icon: ImageBitmap?,
  )

  private var cachedIcon: CachedIcon? = null

  override fun provide(query: String): Flow<List<Action>> {
    return if (configuration.shortcut != null) {
      if (query.startsWith(configuration.shortcut + " ", ignoreCase = true)) {
        val query = query.removePrefix(configuration.shortcut + " ").trim()
        if (configuration.icon is Icon.Url) {
          if (cachedIcon == null) {
            flow {
              // Quickly emit the results without the icon
              emit(listOf(WebSearchAction(configuration, query)))

              // Download the icon
              downloadIcon(configuration.icon.url)

              // Re-emit the result with the cached icon
              emit(listOf(WebSearchAction(configuration, query, iconBitmap = cachedIcon!!.icon)))
            }
          } else {
            flowOf(listOf(WebSearchAction(configuration, query, iconBitmap = cachedIcon!!.icon)))
          }
        } else {
          flowOf(listOf(WebSearchAction(configuration, query)))
        }
      } else {
        flowOf(emptyList())
      }
    } else {
      if (query.isBlank()) {
        flowOf(emptyList())
      } else {
        flowOf(listOf(WebSearchAction(configuration, query)))
      }
    }
  }

  private val httpClient by lazy {
    HttpClient()
  }

  private suspend fun downloadIcon(url: String) {
    runCatching {
      val response: HttpResponse = httpClient.get(url)
      if (!response.status.isSuccess()) {
        logw("Failed to download icon at $url: ${response.status.description}")
        cachedIcon = CachedIcon(null)
      } else {
        val bytes = response.readRawBytes()
        val icon = bytes.decodeToImageBitmap()
        cachedIcon = CachedIcon(icon)
      }
    }.onFailure {
      logw(it, "Failed to download icon at $url")
      cachedIcon = CachedIcon(null)
    }
  }

  data class Configuration(
    val name: String,
    val shortcut: String?,
    val primaryTextPattern: String,
    val urlPattern: String,
    val icon: Icon,
  ) {
    sealed interface Icon {
      class Bundled {
        object Google : Icon
      }

      class Url(val url: String) : Icon
    }
  }
}

private data class WebSearchAction(
  private val configuration: WebSearchActionProvider.Configuration,
  private val query: String,
  private val iconBitmap: ImageBitmap? = null,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:${configuration.name}:$query"
  override val primaryText: String = configuration.primaryTextPattern.replace("{}", query)
  override val secondaryText: String = configuration.name
  override val icon = when (configuration.icon) {
    Icon.Bundled.Google -> Action.Icon.ResourceIcon(Res.drawable.google)
    is Icon.Url -> iconBitmap?.let { Action.Icon.PainterIcon(BitmapPainter(it)) }
  }

  override suspend fun execute() {
    openUrl(configuration.urlPattern.replace("{}", query.urlEncoded()))
  }
}
