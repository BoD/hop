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

import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.ActionProvider.Result
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.action
import org.jraf.hop.action.util.openUrl
import org.jraf.hop.action.util.urlEncoded
import org.jraf.hop.action.websearch.WebSearchActionProvider.Configuration.Icon
import org.jraf.hop.action_websearch.generated.resources.Res
import org.jraf.hop.action_websearch.generated.resources.google

class WebSearchActionProvider(
  private val configuration: Configuration,
) : ActionProvider {
  override fun provide(query: String): Result {
    return if (configuration.shortcut != null) {
      if (query.startsWith(configuration.shortcut + " ", ignoreCase = true)) {
        val query = query.removePrefix(configuration.shortcut + " ").trim()
        if (query.isBlank()) {
          Result.Empty
        } else {
          action(WebSearchAction(configuration, query))
        }
      } else {
        Result.Empty
      }
    } else {
      if (query.isBlank()) {
        Result.Empty
      } else {
        action(WebSearchAction(configuration, query))
      }
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
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:${configuration.name}:$query"
  override val primaryText: String = configuration.primaryTextPattern.replace("{}", query)
  override val secondaryText: String = configuration.name
  override val icon = when (configuration.icon) {
    Icon.Bundled.Google -> Action.Icon.ResourceIcon(Res.drawable.google)
    is Icon.Url -> Action.Icon.UriIcon(configuration.icon.url)
  }

  override suspend fun execute() {
    openUrl(configuration.urlPattern.replace("{}", query.urlEncoded()))
  }
}
