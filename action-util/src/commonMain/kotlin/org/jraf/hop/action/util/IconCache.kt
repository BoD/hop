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

package org.jraf.hop.action.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import org.jraf.klibnanolog.logw

val iconCache: IconCache by lazy { IconCache() }

class IconCache {
  private val cache = mutableMapOf<String, ImageBitmap?>()

  fun isCached(url: String) = cache.containsKey(url)

  suspend fun get(url: String): ImageBitmap? {
    if (cache.containsKey(url)) return cache[url]
    downloadIcon(url)
    return cache[url]
  }

  private suspend fun downloadIcon(url: String) {
    suspendRunCatching {
      val response: HttpResponse = httpClient.get(url)
      if (!response.status.isSuccess()) {
        logw("Failed to download icon at $url: ${response.status.description}")
        cache[url] = null
      } else {
        val bytes = response.readRawBytes()
        val icon = bytes.decodeToImageBitmap()
        cache[url] = icon
      }
    }.onFailure {
      logw(it, "Failed to download icon at $url")
      cache[url] = null
    }
  }
}
