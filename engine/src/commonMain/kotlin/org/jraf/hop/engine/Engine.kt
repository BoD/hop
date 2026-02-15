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

package org.jraf.hop.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.jraf.hop.engine.action.Action
import org.jraf.hop.engine.action.ActionProvider
import org.jraf.hop.engine.action.app.AppActionProvider
import org.jraf.hop.engine.action.websearch.WebSearchActionProvider

@OptIn(ExperimentalCoroutinesApi::class)
class Engine {
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  val query: MutableStateFlow<String> = MutableStateFlow("")

  private val actionProviders: List<ActionProvider> = listOf(
    AppActionProvider(),
    WebSearchActionProvider(),
  )

  val actions: Flow<List<Action>> = query
    .map { query -> query.trim() }
    .distinctUntilChanged()
    .flatMapLatest { query ->
      if (query.isBlank()) {
        flowOf(emptyList())
      } else {
        // TODO We look only at the first emission of each provider!
        flowOf(actionProviders.flatMap { it.provide(query).first() })
      }
    }
    .shareIn(coroutineScope, SharingStarted.Lazily)

  fun executeAction(action: Action) {
    coroutineScope.launch {
      action.execute()
    }
  }
}
