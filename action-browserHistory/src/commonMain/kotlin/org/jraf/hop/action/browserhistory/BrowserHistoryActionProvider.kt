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

package org.jraf.hop.action.browserhistory

import kotlinx.io.files.Path
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.ActionProvider.Result
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.actions
import org.jraf.hop.action.browserhistory.BrowserHistoryActionProvider.Configuration.Icon
import org.jraf.hop.action.util.openUrl
import org.jraf.hop.action_browserhistory.generated.resources.Res
import org.jraf.hop.action_browserhistory.generated.resources.brave

class BrowserHistoryActionProvider(
  private val configuration: Configuration,
) : ActionProvider {
  private val browserHistoryRepository = BrowserHistoryRepository(configuration.historyFilePath)

  override fun provide(query: String): Result {
    return actions {
      val historyItem = browserHistoryRepository.getHistoryItems(query)
      emit(historyItem.map { historyItem -> BrowserHistoryAction(configuration, historyItem) })
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

  private val Configuration.historyFilePath get() = Path(pathToBrowser, "Default", "History")
}

private data class BrowserHistoryAction(
  private val configuration: BrowserHistoryActionProvider.Configuration,
  private val historyItem: BrowserHistoryRepository.HistoryItem,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:$historyItem"
  override val primaryText: String = historyItem.title
  override val secondaryText: String = historyItem.url
  override val icon = when (configuration.icon) {
    Icon.Bundled.Brave -> Action.Icon.ResourceIcon(Res.drawable.brave)
    is Icon.Url -> Action.Icon.UriIcon(configuration.icon.url)
  }

  override suspend fun execute() {
    openUrl(historyItem.url)
  }
}
