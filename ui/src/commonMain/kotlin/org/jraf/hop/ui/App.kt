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

package org.jraf.hop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jraf.hop.engine.Engine
import org.jraf.hop.engine.action.Action

@Composable
fun App(
  engine: Engine,
  focusRequester: FocusRequester,
  onDispose: () -> Unit,
) {
  val viewModel = remember { AppViewModel(engine) }
  val state: AppViewModel.State by viewModel.state.collectAsState()
  Box(
    Modifier
      .fillMaxWidth(),
  ) {
    Column {
      QueryField(
        queryText = state.query,
        onQueryChanged = viewModel::setQuery,
        onKeyboardDown = viewModel::selectNextAction,
        onKeyboardUp = viewModel::selectPreviousAction,
        onKeyboardEscape = { onDispose() },
        onKeyboardEnter = {
          viewModel.executeAction()
          onDispose()
        },
        focusRequester = focusRequester,
      )

      Column(
        modifier = Modifier
          .background(color = Color.LightGray),
      ) {
        for (action in state.actions) {
          val selected = state.selectedAction == action
          Action(action, selected)
        }
      }
    }
  }
}

@Composable
private fun QueryField(
  queryText: String,
  onQueryChanged: (String) -> Unit,
  onKeyboardDown: () -> Unit,
  onKeyboardUp: () -> Unit,
  onKeyboardEscape: () -> Unit,
  onKeyboardEnter: () -> Unit,
  focusRequester: FocusRequester,
) {
  var queryFieldValue by remember { mutableStateOf(TextFieldValue(queryText)) }
  LaunchedEffect(queryText) {
    if (queryText != queryFieldValue.text) {
      queryFieldValue = TextFieldValue(queryText, TextRange(queryText.length))
    }
  }
  BasicTextField(
    modifier = Modifier
      .background(color = Color.White)
      .padding(4.dp)
      .fillMaxWidth()
      .focusRequester(focusRequester)
      .onPreviewKeyEvent {
        when (it.key) {
          Key.DirectionDown -> {
            if (it.type == KeyEventType.KeyDown) {
              onKeyboardDown()
            }
            true
          }

          Key.DirectionUp -> {
            if (it.type == KeyEventType.KeyDown) {
              onKeyboardUp()
            }
            true
          }

          Key.Escape -> {
            if (it.type == KeyEventType.KeyDown) {
              onKeyboardEscape()
            }
            true
          }

          Key.Enter -> {
            if (it.type == KeyEventType.KeyDown) {
              onKeyboardEnter()
            }
            true
          }

          else -> {
            false
          }
        }
      },
    textStyle = LocalTextStyle.current.copy(fontSize = 32.sp),
    value = queryFieldValue,
    singleLine = true,
    onValueChange = { textFieldValue ->
      queryFieldValue = textFieldValue
      onQueryChanged(textFieldValue.text)
    },
  )
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
}


@Composable
private fun Action(action: Action, selected: Boolean) {
  val colors = if (selected) {
    ListItemDefaults.colors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
  } else {
    ListItemDefaults.colors()
  }
  ListItem(
    modifier = Modifier
      .fillMaxWidth(),
    headlineContent = {
      Text(
        text = action.primaryText,
      )
    },
    supportingContent = {
      Text(
        text = action.secondaryText ?: "",
      )
    },
    colors = colors,
  )
}
