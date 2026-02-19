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

package org.jraf.hop.action.bookmark

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class Bookmarks(
  val roots: Map<String, Node>,
)

@Serializable
internal sealed interface Node {
  val type: String
  val name: String
}

@Serializable
@SerialName("url")
internal data class UrlNode(
  override val type: String,
  override val name: String,
  val url: String,
) : Node

@Serializable
@SerialName("folder")
internal data class FolderNode(
  override val type: String,
  override val name: String,
  val children: List<Node>,
) : Node

private fun Bookmarks.flatten(): List<UrlNode> {
  return roots.values.flatMap { it.flatten() }
}

private fun Node.flatten(): List<UrlNode> {
  return when (this) {
    is UrlNode -> listOf(this)
    is FolderNode -> children.flatMap { it.flatten() }
  }
}

private val json = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
}

internal suspend fun loadBookmarks(bookmarkFilePath: Path): List<UrlNode> {
  val bookmarkJsonText = withContext(Dispatchers.IO) {
    SystemFileSystem.source(Path(bookmarkFilePath)).buffered().use { it.readString() }
  }
  val bookmarks: Bookmarks = json.decodeFromString(bookmarkJsonText)
  return bookmarks.flatten().distinct()
}
