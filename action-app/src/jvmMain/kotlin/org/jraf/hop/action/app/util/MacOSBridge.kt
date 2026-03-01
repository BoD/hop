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
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo

private interface MacOSBridge : Library {
  fun getAppIconPixels(
    path: String,
    size: Int,
    outData: PointerByReference,
    outWidth: IntByReference,
    outHeight: IntByReference,
  ): Int

  fun getAllApplicationPaths(): Pointer

  fun freeBuffer(buffer: Pointer)

  companion object {
    val INSTANCE: MacOSBridge = Native.load(
      "libMacOSBridge.dylib",
      MacOSBridge::class.java,
    )
  }
}

internal fun getAppIcon(appPath: String, size: Int = 80): ImageBitmap? {
  val bridge = MacOSBridge.INSTANCE
  val dataRef = PointerByReference()
  val widthRef = IntByReference()
  val heightRef = IntByReference()
  val getAppIconPixelsResult = bridge.getAppIconPixels(
    path = appPath,
    size = size,
    outData = dataRef,
    outWidth = widthRef,
    outHeight = heightRef,
  )
  if (getAppIconPixelsResult == 0) return null

  val height = heightRef.value
  val width = widthRef.value
  val data = dataRef.value.getByteArray(0, width * height * 4)
  bridge.freeBuffer(dataRef.value)

  // Convert RGBA byte array to BGRA format expected by Skia
  val pixels = ByteArray(width * height * 4)
  for (i in 0 until width * height) {
    val r = data[i * 4].toInt() and 0xFF
    val g = data[i * 4 + 1].toInt() and 0xFF
    val b = data[i * 4 + 2].toInt() and 0xFF
    val a = data[i * 4 + 3].toInt() and 0xFF
    pixels[i * 4] = b.toByte()
    pixels[i * 4 + 1] = g.toByte()
    pixels[i * 4 + 2] = r.toByte()
    pixels[i * 4 + 3] = a.toByte()
  }

  val bitmap = Bitmap()
  bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.UNPREMUL))
  bitmap.installPixels(pixels)
  return bitmap.asComposeImageBitmap()
}

internal fun getAllApplicationPaths(): List<String> {
  val bridge = MacOSBridge.INSTANCE
  val pointer = bridge.getAllApplicationPaths()
  val result = pointer.getString(0)
  bridge.freeBuffer(pointer)
  return result.split("\n").filter { it.isNotEmpty() }
}
