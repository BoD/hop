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

package org.jraf.hop.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jraf.hop.engine.Engine
import org.jraf.hop.engine.action.Action

class AppViewModel(
  private val engine: Engine,
) {
  data class State(
    val query: String,
    val actions: List<Action>,
    val selectedAction: Action?,
  )

  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private val selectedAction = MutableStateFlow<Action?>(null)

  val state: StateFlow<State> = combine(engine.query, engine.actions, selectedAction) { query, actions, selectedAction ->
    State(
      query = query,
      actions = actions,
      selectedAction = selectedAction ?: actions.firstOrNull(),
    )
  }
    .stateIn(
      coroutineScope, SharingStarted.Lazily,
      State(
        query = "",
        actions = emptyList(),
        selectedAction = null,
      ),
    )

  fun setQuery(query: String) {
//    val query = query.trim()
//    if (query == engine.query.value) return
    selectedAction.value = null
    engine.query.value = query
  }

  fun selectPreviousAction() {
    moveSelectedAction(-1)
  }

  fun selectNextAction() {
    moveSelectedAction(1)
  }

  private fun moveSelectedAction(direction: Int) {
    val actions = state.value.actions
    val currentIndex = actions.indexOf(state.value.selectedAction)
    this.selectedAction.value = actions[if (currentIndex == -1) 0 else (currentIndex + direction).mod(actions.size)]
  }

  fun executeAction() {
    engine.executeAction(state.value.selectedAction!!)
  }
}
