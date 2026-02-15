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

package org.jraf.hop.engine.action.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.jraf.hop.engine.action.Action
import org.jraf.hop.engine.action.ActionProvider
import org.jraf.hop.engine.util.listFilesInDir
import org.jraf.hop.engine.util.openApplication

class AppActionProvider : ActionProvider {
  override fun provide(query: String): Flow<List<Action>> {
    return flow {
      val matchingFiles = withContext(Dispatchers.IO) {
        listFilesInDir("/Applications")
      }.filter {
        val fileName = it.split("/").last()
        fileName.endsWith(".app") && fileName.contains(query, ignoreCase = true)
      }
      emit(
        matchingFiles.map { file ->
          AppAction(file)
        }.sortedBy { it.primaryText },
      )
    }
  }
}

data class AppAction(val applicationFile: String) : Action {
  override val primaryText: String
  override val secondaryText: String?

  init {
    val fileName = applicationFile.split("/").last()
    val appName = fileName.removeSuffix(".app")

    primaryText = appName
    secondaryText = applicationFile
  }

  override suspend fun execute() {
    openApplication(applicationFile)
  }
}
