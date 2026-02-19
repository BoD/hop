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

import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource

interface Action {
  /**
   * Must be globally unique.
   * By convention, it should start with the implementation's package name.
   */
  val id: String
  val primaryText: String
  val secondaryText: String?
  val icon: Icon?

  suspend fun execute()

  sealed interface Icon {
    class PainterIcon(val painter: Painter) : Icon
    class ResourceIcon(val resource: DrawableResource) : Icon
  }
}


abstract class BaseAction : Action {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BaseAction) return false
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
