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

package org.jraf.hop.desktopapp

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tulskiy.keymaster.common.Provider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jraf.hop.action.Action
import org.jraf.hop.action.app.AppActionProvider
import org.jraf.hop.action.bookmark.BookmarkActionProvider
import org.jraf.hop.action.url.UrlActionProvider
import org.jraf.hop.action.websearch.WebSearchActionProvider
import org.jraf.hop.desktopapp.generated.resources.Res
import org.jraf.hop.desktopapp.generated.resources.app_name
import org.jraf.hop.desktopapp.util.getScreenSize
import org.jraf.hop.desktopapp.util.getWindowPosition
import org.jraf.hop.engine.Engine
import org.jraf.hop.ui.ActionItemHeight
import org.jraf.hop.ui.App
import org.jraf.hop.ui.QueryFieldHeight
import org.jraf.hop.ui.generated.resources.hop
import java.awt.Desktop
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

private val WindowWidth = 640.dp

fun main() {
  System.setProperty("apple.awt.UIElement", "true")
  System.setProperty("apple.awt.enableTemplateImages", "true")

  val hotKeyProvider = Provider.getCurrentProvider(false)
  val screenSize = getScreenSize()
  val engine = Engine(
    listOf(
      AppActionProvider(),
      UrlActionProvider(
        UrlActionProvider.Configuration(
          icon = UrlActionProvider.Configuration.Icon.Bundled.Brave,
        ),
      ),
      BookmarkActionProvider(
        BookmarkActionProvider.Configuration(
          pathToBrowser = "/Users/bod/Library/Application Support/BraveSoftware/Brave-Browser",
          icon = BookmarkActionProvider.Configuration.Icon.Bundled.Brave,
        ),
      ),
      WebSearchActionProvider(
        WebSearchActionProvider.Configuration(
          name = "GitHub",
          shortcut = "gh",
          primaryTextPattern = "Search for '{}'",
          urlPattern = "https://github.com/search?q={}&type=code",
          icon = WebSearchActionProvider.Configuration.Icon.Url("https://github.githubassets.com/favicons/favicon.png"),
        ),
      ),
      WebSearchActionProvider(
        WebSearchActionProvider.Configuration(
          name = "Apollo Kotlin",
          shortcut = "pr",
          primaryTextPattern = "Open Apollo Kotlin PR #{}",
          urlPattern = "https://github.com/apollographql/apollo-kotlin/pull/{}",
          icon = WebSearchActionProvider.Configuration.Icon.Url("https://avatars.githubusercontent.com/u/17189275?s=48&v=4"),
        ),
      ),
      WebSearchActionProvider(
        WebSearchActionProvider.Configuration(
          name = "Google",
          shortcut = null,
          primaryTextPattern = "Search for '{}'",
          urlPattern = "https://www.google.com/search?q={}",
          icon = WebSearchActionProvider.Configuration.Icon.Bundled.Google,
        ),
      ),
    ),
  )
  application {
    var isVisible by remember { mutableStateOf(true) }
    // Global hot-key
    hotKeyProvider.register(KeyStroke.getKeyStroke("meta SPACE")) {
      isVisible = !isVisible
    }

    val windowState = rememberWindowState(
      size = DpSize(WindowWidth, QueryFieldHeight),
      position = getWindowPosition(WindowWidth, QueryFieldHeight, .33F),
    )
    if (isVisible) {
      Window(
        state = windowState,
        undecorated = true,
        transparent = true,
        title = stringResource(Res.string.app_name),
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
          } else {
            // Reset query when hiding the window
            engine.query.value = ""
          }
        }
      }
    }

    Tray(
      painterResource(org.jraf.hop.ui.generated.resources.Res.drawable.hop),
      tooltip = stringResource(Res.string.app_name),
      onAction = {
        isVisible = !isVisible
      },
      menu = {
        Item("Exit", onClick = ::exitApplication)
      },
    )
  }
}
