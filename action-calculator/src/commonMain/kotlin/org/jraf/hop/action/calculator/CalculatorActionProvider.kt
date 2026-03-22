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

package org.jraf.hop.action.calculator

import org.jraf.hop.action.Action
import org.jraf.hop.action.ActionProvider
import org.jraf.hop.action.ActionProvider.Result
import org.jraf.hop.action.BaseAction
import org.jraf.hop.action.action
import org.jraf.hop.action_calculator.generated.resources.Res
import org.jraf.hop.action_calculator.generated.resources.calculator

class CalculatorActionProvider : ActionProvider {
  override fun provide(query: String): Result {
    if (!query.isMathExpression()) {
      return Result.Empty
    }
    val result = runCatching { eval(query.stripSpaces()) }.getOrNull()
    return if (result == null) {
      Result.Empty
    } else {
      action(CalculatorAction(expression = query, result = result.toFormattedString()))
    }
  }

  private fun String.isMathExpression() = contains("\\d".toRegex()) && contains("[+\\-*/()^]".toRegex())

  private fun String.stripSpaces() = "\\s+".toRegex().replace(this, "")

  private fun Double.toFormattedString(): String {
    return if (this % 1.0 == 0.0) {
      this.toLong().toString()
    } else {
      this.toString()
    }
  }
}

private data class CalculatorAction(
  private val expression: String,
  private val result: String,
) : BaseAction() {
  override val id: String = "${this::class.qualifiedName!!}:$expression"
  override val primaryText: String = result
  override val secondaryText: String = expression
  override val icon = Action.Icon.ResourceIcon(Res.drawable.calculator)

  override suspend fun execute() {
    copyToClipboard(result)
  }
}
