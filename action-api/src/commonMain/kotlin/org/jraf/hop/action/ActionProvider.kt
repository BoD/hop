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

package org.jraf.hop.action

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jraf.hop.action.ActionProvider.Result

interface ActionProvider {
  fun provide(query: String): Result

  sealed interface Result {
    val actions: Flow<List<Action>>

    data object Empty : Result {
      override val actions: Flow<List<Action>> = flowOf(emptyList())
    }

    data class Actions(override val actions: Flow<List<Action>>) : Result
  }
}

fun actions(block: suspend FlowCollector<List<Action>>.() -> Unit): Result.Actions {
  return Result.Actions(flow(block))
}

fun actions(actions: List<Action>): Result.Actions {
  return Result.Actions(flowOf(actions))
}

fun action(action: Action): Result.Actions {
  return actions(listOf(action))
}
