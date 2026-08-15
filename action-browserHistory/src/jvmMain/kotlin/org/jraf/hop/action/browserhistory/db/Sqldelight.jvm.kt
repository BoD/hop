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

package org.jraf.hop.action.browserhistory.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.io.files.Path
import java.io.File

internal actual fun createSqlDriver(historyFilePath: Path): SqlDriver {
  // The browser locks the database, but passing these options allows to open it (read only) anyway
  // See https://www.sqlite.org/uri.html#recognized_query_parameters
  // See https://sqlite.org/forum/forumpost/86a67f6995124119
  val fileUri = File(historyFilePath.toString()).toURI().toASCIIString()
  return JdbcSqliteDriver("jdbc:sqlite:$fileUri?mode=ro&nolock=1")
}
