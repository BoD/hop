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

package org.jraf.hop.engine.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val LONG_TERM_HISTORY_SIZE = 600L
private const val LONG_TERM_WEIGHT = 1L

private const val SHORT_TERM_HISTORY_SIZE = 20L
private const val SHORT_TERM_WEIGHT = 3L

internal class LaunchItemRepository() {
  private val database: HopDatabase = HopDatabase(createSqlDriver())

  suspend fun recordLaunchedItem(id: String) {
    withContext(Dispatchers.IO) {
      database.launchedItemsQueries.insert(id)
    }
  }

  internal data class Counter(
    val longTerm: Long,
    val combined: Long,
  )

  val counters: Flow<Map<String, Counter>> = combine(
    getCounters(LONG_TERM_HISTORY_SIZE, LONG_TERM_WEIGHT),
    getCounters(SHORT_TERM_HISTORY_SIZE, SHORT_TERM_WEIGHT),
  ) { longTerm, shortTerm ->
    longTerm.mapValues {
      Counter(
        longTerm = it.value,
        combined = it.value + (shortTerm[it.key] ?: 0),
      )
    }
  }

  private fun getCounters(historySize: Long, weight: Long): Flow<Map<String, Long>> =
    database.launchedItemsQueries.select(historySize = historySize)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { counters ->
        counters.associate { it.id to it.count * weight }
      }
}
