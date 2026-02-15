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

package org.jraf.hop.engine.action.websearch

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jraf.hop.engine.action.Action
import org.jraf.hop.engine.action.ActionProvider
import org.jraf.hop.engine.util.openUrl
import org.jraf.hop.engine.util.urlEncoded

class WebSearchActionProvider : ActionProvider {
  override fun provide(query: String): Flow<List<Action>> {
    return flowOf(listOf(WebSearchAction(query)))
  }
}

data class WebSearchAction(val searchTerm: String) : Action {
  override val primaryText: String = "Search \"$searchTerm\" on Google"
  override val secondaryText: String? = null

  override suspend fun execute() {
    openUrl("https://www.google.com/search?q=${searchTerm.urlEncoded()}")
  }
}
