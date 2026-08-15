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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import org.jraf.hop.action.browserhistory.db.chromium.ChromiumDatabase
import org.jraf.hop.action.browserhistory.db.createSqlDriver

class BrowserHistoryRepository(
  historyFilePath: Path,
) {
  data class HistoryItem(
    val title: String,
    val url: String,
  )

  private val database: ChromiumDatabase = ChromiumDatabase(createSqlDriver(historyFilePath))

  suspend fun getHistoryItems(query: String): List<HistoryItem> {
    return database.urlsQueries
      .select(query = query, historySize = 15)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .first()
      .map {
        HistoryItem(
          title = it.title!!,
          url = it.url!!,
        )
      }
      .distinctBy { it.url.removeSuffix("/") }
  }
}
