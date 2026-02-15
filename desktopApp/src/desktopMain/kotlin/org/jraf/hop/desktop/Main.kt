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

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tulskiy.keymaster.common.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jraf.hop.desktop.util.getScreenSize
import org.jraf.hop.desktop.util.getWindowPosition
import org.jraf.hop.engine.Engine
import org.jraf.hop.engine.action.Action
import org.jraf.hop.ui.App
import java.awt.Desktop
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities


fun main() {
  val screenSize = getScreenSize()
  val engine = Engine()
  val provider = Provider.getCurrentProvider(false)
  application {
//    val windowState = rememberWindowState(size = DpSize.Unspecified, position = WindowPosition(Alignment.Center))
    val windowState = rememberWindowState(size = DpSize(800.dp, 45.5.dp), position = getWindowPosition(800.dp, 45.5.dp, .33F))

    var isVisible by remember { mutableStateOf(true) }

    provider.register(KeyStroke.getKeyStroke("control SPACE")) {
      isVisible = !isVisible
    }

    Window(
      state = windowState,
      undecorated = true,
      transparent = true,
      title = "hop",
      visible = isVisible,
      alwaysOnTop = true,
      onCloseRequest = {
        exitApplication()
      },
    ) {

      val actions: List<Action> by engine.actions.collectAsState(emptyList())
      val density: Density = LocalDensity.current

      val configuration = LocalWindowInfo.current
      val screenHeight = configuration.containerSize


      LaunchedEffect(actions) {
        launch {
          delay(10)
          val prefSize = window.preferredSize.let {
//            DpSize(it.width.dp, it.height.dp)
            val heightBasedOnActions = (45.5 + actions.size * 72).dp
            val maxHeight = screenSize.height - window.location.y.dp
            val height = heightBasedOnActions.coerceAtMost(maxHeight)
            DpSize(it.width.dp, height)
          }
          // Optional animation; can just assign windowState.size immediately instead
//        val animation = Animatable(
//          initialValue = windowState.size,
//          typeConverter = DpSizeConverter
//        )
//        animation.animateTo(prefSize) {
//          windowState.size = value
//        }
          windowState.size = prefSize
        }
      }

//      val windowInfo = LocalWindowInfo.current
//      LaunchedEffect(windowInfo.isWindowFocused) {
////          if (!windowInfo.isWindowFocused) {
////            isVisible = false
////          }
//
//        println("FOCUS = " + windowInfo.isWindowFocused)
//      }

      val focusRequester = remember { FocusRequester() }
      Box(
//        modifier = Modifier
//          .safeContentPadding()
//          .fillMaxSize(),
      ) {
        App(
          engine = engine,
          focusRequester = focusRequester,
          onDispose = {
            isVisible = false
          },
        )
      }

      DisposableEffect(window) {
        val listener = object : WindowAdapter() {
          override fun windowActivated(e: WindowEvent?) {
            SwingUtilities.invokeLater {
              focusRequester.requestFocus()
            }
          }

          override fun windowDeactivated(e: WindowEvent?) {
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

object TrayIcon : Painter() {
  override val intrinsicSize = Size(256f, 256f)

  override fun DrawScope.onDraw() {
    drawOval(Color(0xFFFFA500))
  }
}

private val DpSizeConverter = TwoWayConverter<DpSize, AnimationVector2D>(
  convertToVector = { AnimationVector2D(it.width.value, it.height.value) },
  convertFromVector = { DpSize(it.v1.dp, it.v2.dp) },
)
