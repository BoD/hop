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

package org.jraf.hop.action.app.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.github.gino0631.icns.IcnsIcons
import kotlinx.io.files.Path
import org.jraf.klibnanolog.logw

internal actual suspend fun readIcnsIcon(icnsPath: Path): ImageBitmap? {
  val icons: IcnsIcons = IcnsIcons.load(kotlin.io.path.Path(icnsPath.toString()))
  val icnsIconsEntries = icons.entries.sortedByDescending { it.type?.ordinal ?: -1 }.ifEmpty { return null }
  return icnsIconsEntries.firstNotNullOfOrNull { icnsIconsEntry ->
    if (icnsIconsEntry.type == null) return@firstNotNullOfOrNull null
    val bytes = icnsIconsEntry.newInputStream().use { inputStream -> inputStream.readBytes() }
    runCatching {
      bytes.decodeToImageBitmap()
    }.onFailure {
      logw("Failed to decode ICNS icon at $icnsPath of type ${icnsIconsEntry.type}")
    }
      .getOrNull()
  }
}
