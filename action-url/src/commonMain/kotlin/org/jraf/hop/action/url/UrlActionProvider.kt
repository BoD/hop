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

package org.jraf.hop.action.url

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.url.UrlActionProvider.Configuration.Icon
import org.jraf.hop.action.util.openUrl
import org.jraf.hop.action_url.generated.resources.Res
import org.jraf.hop.action_url.generated.resources.brave

class UrlActionProvider(
  private val configuration: Configuration,
) : ActionProvider {

  override fun provide(query: String): Flow<List<Action>> {
    return if (!query.isProbableUrl()) {
      flowOf(listOf())
    } else {
      flowOf(listOf(UrlAction(configuration, query)))
    }
  }

  data class Configuration(
    val icon: Icon,
  ) {
    sealed interface Icon {
      class Bundled {
        object Brave : Icon
      }

      class Url(val url: String) : Icon
    }
  }

  private val probableUrlRegex = Regex("^(https?://)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*$")

  private fun String.isProbableUrl() = matches(probableUrlRegex)
}

private data class UrlAction(
  private val configuration: UrlActionProvider.Configuration,
  private val url: String,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:$url"
  override val primaryText: String = url
  override val secondaryText: String = "Open in browser"
  override val icon = when (configuration.icon) {
    Icon.Bundled.Brave -> Action.Icon.ResourceIcon(Res.drawable.brave)
    is Icon.Url -> Action.Icon.UriIcon(configuration.icon.url)
  }

  override suspend fun execute() {
    openUrl(url.let { if (!it.startsWith("https://") && !it.startsWith("http://")) "https://$it" else it })
  }
}
