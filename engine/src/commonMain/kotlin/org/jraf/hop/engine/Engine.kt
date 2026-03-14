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

@file:OptIn(FlowPreview::class)

package org.jraf.hop.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.engine.db.LaunchItemRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class Engine(
  private val actionProviders: List<ActionProvider>,
) {
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private val launchItemRepository = LaunchItemRepository()

  val query: MutableStateFlow<String> = MutableStateFlow("")

  val actions: Flow<List<Action>> = combine(
    query
//      .debounce(50.milliseconds)
      .map { query -> query.trimStart() }
      .distinctUntilChanged(),
    launchItemRepository.counters,
  ) { query, counters ->
    query to counters
  }
    .flatMapLatest { (query, counters) ->
      if (query.isBlank()) {
        flowOf(emptyList())
      } else {
        combine(actionProviders.map { it.provide(query).defaultAfterTimeout(300.milliseconds, emptyList()) }) { actionsList ->
          actionsList
            .flatMap { it.map { action -> TrackingAction(action) } }
            .sortedByDescending { counters[it.id]?.combined ?: 0 }
        }
      }
    }
    .shareIn(coroutineScope, SharingStarted.Lazily)

  fun executeAction(action: Action) {
    coroutineScope.launch {
      action.execute()
    }
  }

  private inner class TrackingAction(private val action: Action) : Action by action {
    override suspend fun execute() {
      launchItemRepository.recordLaunchedItem(id)
      action.execute()
    }
  }
}

/**
 * If nothing has been emitted by this Flow after [timeout], start by emitting [defaultValue], and then emit the actual values when they become available.
 */
private fun <T> Flow<T>.defaultAfterTimeout(timeout: Duration, defaultValue: T): Flow<T> {
  return channelFlow {
    var emitted = false
    val timeoutJob = launch {
      delay(timeout)
      if (!emitted) {
        send(defaultValue)
      }
    }
    collect { value ->
      emitted = true
      timeoutJob.cancel()
      send(value)
    }
  }
}
