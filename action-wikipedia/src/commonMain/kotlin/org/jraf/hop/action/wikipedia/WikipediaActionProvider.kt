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

package org.jraf.hop.action.wikipedia

import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.actions
import org.jraf.hop.action.util.openUrl
import org.jraf.hop.action.wikipedia.api.WikipediaApiClient

private const val ICON_URL = "https://en.wikipedia.org/static/apple-touch/wikipedia.png"

class WikipediaActionProvider(
  private val configuration: Configuration,
) : ActionProvider {
  private val wikipediaApiClient = WikipediaApiClient()

  override fun provide(query: String): ActionProvider.Result {
    if (!query.startsWith(configuration.shortcut + " ", ignoreCase = true)) return ActionProvider.Result.Empty
    val query = query.removePrefix(configuration.shortcut + " ").trim()
    if (query.isBlank()) return ActionProvider.Result.Empty
    return actions {
      val actions = wikipediaApiClient.search(query).mapIndexed { index, searchResult ->
        WikipediaAction(
          query = query,
          articleTitle = searchResult.title,
          articleUrl = searchResult.url,
          index = index,
        )
      }
      emit(actions)
    }
  }

  data class Configuration(
    val shortcut: String,
  )
}

private data class WikipediaAction(
  private val query: String,
  private val articleTitle: String,
  private val articleUrl: String,
  private val index: Int,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:$query:$index"
  override val primaryText: String = articleTitle
  override val secondaryText: String = articleUrl
  override val icon = Action.Icon.UriIcon(ICON_URL)

  override suspend fun execute() {
    openUrl(articleUrl)
  }
}
