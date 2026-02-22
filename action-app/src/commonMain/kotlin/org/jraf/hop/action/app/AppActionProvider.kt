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

package org.jraf.hop.action.app

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.util.openApplication

class AppActionProvider : ActionProvider {
  private val macOSAppIconCache = MacOSAppIconCache()

  override fun provide(query: String): Flow<List<Action>> {
    return flow {
      val matchingFiles = withContext(Dispatchers.IO) {
        SystemFileSystem.list(Path("/Applications")) +
          SystemFileSystem.list(Path("/System/Applications"))
      }.filter { path ->
        path.name.endsWith(".app") && path.name.contains(query, ignoreCase = true)
      }
      emit(
        matchingFiles.map { path ->
          val icon = macOSAppIconCache.getIcon(path)
          AppAction(path, icon)
        }.sortedBy { it.primaryText },
      )
    }
  }
}

private data class AppAction(
  private val applicationPath: Path,
  private val iconBitmap: ImageBitmap?,
) : BaseAction() {
  override val id: String = this::class.qualifiedName!! + ":" + applicationPath
  override val primaryText: String
  override val secondaryText: String?
  override val icon: Action.Icon?

  init {
    val appName = applicationPath.name.removeSuffix(".app")
    primaryText = appName
    secondaryText = applicationPath.toString()
    icon = iconBitmap?.let { Action.Icon.PainterIcon(BitmapPainter(it)) }
  }

  override suspend fun execute() {
    openApplication(applicationPath)
  }
}
