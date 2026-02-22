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
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.getiox.plist.PList
import com.getiox.plist.array
import com.getiox.plist.dict
import com.getiox.plist.string
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jraf.hop.action.app.util.readIcnsIcon
import org.jraf.hop.action.util.suspendRunCatching
import org.jraf.klibnanolog.logd
import org.jraf.klibnanolog.logw

internal class MacOSAppIconCache {
  private val cache = mutableMapOf<Path, ImageBitmap?>()

  suspend fun getIcon(applicationPath: Path): ImageBitmap? {
    if (cache.containsKey(applicationPath)) {
      return cache[applicationPath]
    }
    return withContext(Dispatchers.IO) {
      val wrappedBundlePath = Path(applicationPath, "WrappedBundle")
      val imageBitmap = if (SystemFileSystem.exists(wrappedBundlePath)) {
        // Well this doesn't actually work, don't know why. This returns a transparent icon. Maybe a permissions problem?
        // getIconFromWrappedBundle(wrappedBundlePath)
        null
      } else {
        getIconFromBundle(applicationPath)
      }
      cache[applicationPath] = imageBitmap
      imageBitmap
    }
  }

  private fun getIconFromWrappedBundle(wrappedBundlePath: Path): ImageBitmap? {
    val pListValue = runCatching {
      PList.decode(
        SystemFileSystem.source(Path(wrappedBundlePath, "Info.plist")).buffered().use { it.readByteArray() },
      )
    }.getOrElse {
      logw("Failed to read Info.plist at $wrappedBundlePath: ${it.message}")
      return null
    }
    val iconFiles: List<String> =
      pListValue.dict["CFBundleIcons"]?.dict["CFBundlePrimaryIcon"]?.dict?.get("CFBundleIconFiles")?.array?.map { it.string }
        ?.reversed()
        ?.flatMap {
          listOf(
            "$it@2x.png",
            "$it.png",
          )
        } ?: run {
        logw("Failed to find CFBundleIconFiles in Info.plist at $wrappedBundlePath")
        return null
      }
    return iconFiles.firstNotNullOfOrNull {
      val iconFilePath = Path(wrappedBundlePath, it)
      logd(iconFilePath)
      runCatching {
        val bytes = SystemFileSystem.source(iconFilePath).buffered().use { source -> source.readByteArray() }
        bytes.decodeToImageBitmap()
      }.onFailure {
        logw("Failed to decode icon at $iconFilePath")
      }.getOrNull()
    }
  }

  private suspend fun getIconFromBundle(applicationPath: Path): ImageBitmap? {
    val pListValue = suspendRunCatching {
      PList.decode(
        SystemFileSystem.source(Path(applicationPath, "Contents", "Info.plist")).buffered().use { it.readByteArray() },
      )
    }.getOrElse {
      logw("Failed to read Info.plist at $applicationPath: ${it.message}")
      return null
    }
    val iconFileName = pListValue.dict["CFBundleIconFile"]?.string ?: run {
      logw("Failed to find CFBundleIconFile in Info.plist at $applicationPath")
      return null
    }
    val iconFilePath = "$applicationPath/Contents/Resources/$iconFileName".let {
      if (!it.endsWith(".icns")) {
        "$it.icns"
      } else {
        it
      }
    }
    return readIcnsIcon(Path(iconFilePath))
  }
}
