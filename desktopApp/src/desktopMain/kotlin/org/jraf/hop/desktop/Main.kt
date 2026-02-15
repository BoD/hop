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

package org.jraf.hop.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tulskiy.keymaster.common.Provider
import org.jraf.hop.desktop.util.getScreenSize
import org.jraf.hop.desktop.util.getWindowPosition
import org.jraf.hop.engine.Engine
import org.jraf.hop.engine.action.Action
import org.jraf.hop.ui.ActionItemHeight
import org.jraf.hop.ui.App
import org.jraf.hop.ui.QueryFieldHeight
import java.awt.Desktop
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

private val WindowWidth = 800.dp

fun main() {
  val hotKeyProvider = Provider.getCurrentProvider(false)
  val screenSize = getScreenSize()
  val engine = Engine()
  application {
    MaterialTheme(if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {

      var isVisible by remember { mutableStateOf(true) }
      // Global hot-key
      hotKeyProvider.register(KeyStroke.getKeyStroke("control SPACE")) {
        isVisible = !isVisible
      }

      val windowState = rememberWindowState(
        size = DpSize(WindowWidth, QueryFieldHeight),
        position = getWindowPosition(WindowWidth, QueryFieldHeight, .33F),
      )
      Window(
        state = windowState,
        undecorated = true,
        transparent = true,
        title = "hop",
        visible = isVisible,
        onCloseRequest = {
          exitApplication()
        },
      ) {
        // Adjust the size of the window based on the number of actions
        val actions: List<Action> by engine.actions.collectAsState(emptyList())
        LaunchedEffect(actions) {
          val heightBasedOnActions = QueryFieldHeight + actions.size * ActionItemHeight
          val maxHeight = screenSize.height - window.location.y.dp
          val height = heightBasedOnActions.coerceAtMost(maxHeight)
          windowState.size = DpSize(window.preferredSize.width.dp, height)
        }

        val focusRequester = remember { FocusRequester() }
        App(
          engine = engine,
          focusRequester = focusRequester,
          onDispose = {
            isVisible = false
          },
        )

        // See https://youtrack.jetbrains.com/issue/CMP-4231
        DisposableEffect(window) {
          val listener = object : WindowAdapter() {
            override fun windowActivated(e: WindowEvent) {
              SwingUtilities.invokeLater {
                focusRequester.requestFocus()
              }
            }

            override fun windowDeactivated(e: WindowEvent) {
              isVisible = false
              // Reset query when hiding the window
              engine.query.value = ""
            }
          }
          window.addWindowListener(listener)
          onDispose {
            window.removeWindowListener(listener)
          }
        }

        LaunchedEffect(isVisible) {
          if (isVisible) {
            Desktop.getDesktop().requestForeground(true)
          }
        }
      }

      Tray(
        TrayIcon,
        tooltip = "Counter",
        onAction = {
          isVisible = !isVisible
        },
        menu = {
          Item("Exit", onClick = ::exitApplication)
        },
      )
    }
  }
}

object TrayIcon : Painter() {
  override val intrinsicSize = Size(64f, 64f)

  override fun DrawScope.onDraw() {
    drawOval(Color(0xFFFFFF00))
  }
}
