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

package org.jraf.hop.desktop.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import java.awt.GraphicsEnvironment
import java.awt.Insets
import java.awt.Toolkit

fun getScreenSize(accountInsets: Boolean = false): DpSize {
  // Note, since the Compose density is equal to the AWT scaleX, we don't need to multiply by graphicsConfiguration.defaultTransform.scaleX to get dp values.
  // The values unscaled are already in dp.
  val graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
  val screenInsets: Insets = if (accountInsets) {
    Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
  } else {
    Insets(0, 0, 0, 0)
  }
  return DpSize(
    (graphicsConfiguration.bounds.width - screenInsets.left - screenInsets.right).dp,
    (graphicsConfiguration.bounds.height - screenInsets.top - screenInsets.bottom).dp,
  )
}

fun getWindowPosition(
  width: Dp,
  height: Dp,
  topPercent: Float,
):WindowPosition {
  val screenSize = getScreenSize(accountInsets = true)
  val x = (screenSize.width - width) / 2
  val y = (screenSize.height - height) * topPercent
  return WindowPosition(x, y)
}
