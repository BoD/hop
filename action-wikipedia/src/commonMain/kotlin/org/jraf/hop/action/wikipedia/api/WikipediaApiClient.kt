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

package org.jraf.hop.action.wikipedia.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class WikipediaApiClient {
  private val httpClient: HttpClient = HttpClient {
    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          useAlternativeNames = false
        },
      )
    }
//    install(Logging) {
//      logger = object : Logger {
//        override fun log(message: String) {
//          logd("Ktor - $message")
//        }
//      }
//      level = LogLevel.ALL
//    }
  }

  suspend fun search(query: String): List<SearchResult> {
    val jsonResult: JsonArray = httpClient.get("https://en.wikipedia.org/w/api.php") {
      parameter("action", "opensearch")
      parameter("search", query)
      parameter("limit", "5")
    }.body()
    val jsonTitles = jsonResult[1].jsonArray
    val jsonUrls = jsonResult[3].jsonArray
    return jsonTitles.zip(jsonUrls) { title, url ->
      SearchResult(title.jsonPrimitive.content, url.jsonPrimitive.content)
    }
  }

  internal data class SearchResult(
    val title: String,
    val url: String,
  )
}
