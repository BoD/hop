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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.DynamicMaterialTheme
import org.jetbrains.compose.resources.painterResource
import org.jraf.hop.action.Action
import org.jraf.hop.engine.Engine
import org.jraf.hop.ui.generated.resources.Res
import org.jraf.hop.ui.generated.resources.hop

@Composable
fun App(
  engine: Engine,
  focusRequester: FocusRequester,
  onDispose: () -> Unit,
) {
  DynamicMaterialTheme(seedColor = systemAccentColor() ?: Color(red = 232, green = 136, blue = 58)) {
    val actionItemHeightPx = with(LocalDensity.current) { ActionItemHeight.toPx() }
    val viewModel = remember { AppViewModel(engine) }
    val state: AppViewModel.State by viewModel.state.collectAsState()
    Column(
      Modifier
        .fillMaxWidth(),
    ) {
      QueryField(
        queryText = state.query,
        onQueryChanged = viewModel::setQuery,
        onKeyboardDown = viewModel::selectNextAction,
        onKeyboardUp = viewModel::selectPreviousAction,
        onKeyboardEscape = { onDispose() },
        onKeyboardEnter = {
          viewModel.executeSelectedAction()
          onDispose()
        },
        focusRequester = focusRequester,
      )

      val lazyListState = rememberLazyListState()
      LazyColumn(
        modifier = Modifier
          .background(color = MaterialTheme.colorScheme.surfaceContainerHighest),
        state = lazyListState,
      ) {
        items(state.actions, key = { it.id }) { action ->
          val bringIntoViewRequester = remember { BringIntoViewRequester() }
          Box(
            modifier = Modifier
              .bringIntoViewRequester(bringIntoViewRequester),
          ) {
            val isSelected = state.selectedAction == action
            if (isSelected) {
              LaunchedEffect(Unit) {
                bringIntoViewRequester.bringIntoView(Rect(Offset(0F, -actionItemHeightPx), Size(0F, actionItemHeightPx * 3)))
              }
            }
            ActionItem(
              action = action,
              selected = isSelected,
              onActionClick = {
                viewModel.executeAction(it)
                onDispose()
              },
            )
          }
        }
      }
      LaunchedEffect(state.selectedAction) {
        val selectedActionIndex = state.actions.indexOf(state.selectedAction)
        if (selectedActionIndex != -1) {
          val visible = lazyListState.layoutInfo.visibleItemsInfo.any { it.index == selectedActionIndex }
          if (!visible) {
            lazyListState.scrollToItem(selectedActionIndex)
          }
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
  val textColor = LocalTextStyle.current.color.takeOrElse { OutlinedTextFieldDefaults.colors().focusedTextColor }
  val mergedTextStyle = LocalTextStyle.current.merge(
    TextStyle(
      color = textColor,
      fontSize = 32.sp,
      lineHeight = 32.sp,
    ),
  )

  Box(
    modifier = Modifier
      .border(
        border = BorderStroke(2.dp, SolidColor(MaterialTheme.colorScheme.primary)),
        shape = OutlinedTextFieldDefaults.shape,
      )
      .padding(1.dp)
      .fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .background(color = MaterialTheme.colorScheme.surface, shape = OutlinedTextFieldDefaults.shape),
    ) {
      BasicTextField(
        modifier = Modifier
          .padding(8.dp)
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
        textStyle = mergedTextStyle,
        value = queryFieldValue,
        singleLine = true,
        cursorBrush = SolidColor(OutlinedTextFieldDefaults.colors().cursorColor),
        onValueChange = { textFieldValue ->
          queryFieldValue = textFieldValue
          onQueryChanged(textFieldValue.text)
        },
      )
      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }
    }
  }
}

@Composable
private fun ActionItem(
  action: Action,
  selected: Boolean,
  onActionClick: (Action) -> Unit,
) {
  val colors = if (selected) {
    ListItemDefaults.colors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
  } else {
    ListItemDefaults.colors()
  }
  ListItem(
    modifier = Modifier
      .clickable(onClick = { onActionClick(action) })
      .heightIn(min = ActionItemHeight)
      .fillMaxWidth(),
    headlineContent = {
      Text(
        text = action.primaryText,
        maxLines = 1,
      )
    },
    supportingContent = action.secondaryText?.let {
      {
        Text(
          text = action.secondaryText!!,
          maxLines = 1,
        )
      }
    },
    leadingContent = {
      val icon = action.icon
      if (icon == null) {
        Icon(
          modifier = Modifier.size(40.dp),
          painter = painterResource(Res.drawable.hop),
          contentDescription = null,
          tint = LocalContentColor.current.copy(alpha = 0.38f),
        )
      } else {
        Image(
          modifier = Modifier.size(40.dp),
          painter = when (icon) {
            is Action.Icon.PainterIcon -> icon.painter
            is Action.Icon.ResourceIcon -> painterResource(icon.resource)
          },
          contentDescription = null,
        )
      }
    },
    colors = colors,
  )
}
