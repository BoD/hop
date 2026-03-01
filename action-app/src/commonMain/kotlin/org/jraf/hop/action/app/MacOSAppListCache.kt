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

package org.jraf.hop.action.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import org.jraf.hop.action.app.util.getMacOSAllApplications
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val CACHE_DURATION = 15.seconds

internal class MacOSAppListCache {
  private var cachedApps: Set<Path>? = null
  private var cacheDate: Instant? = null

  internal suspend fun getAllApps(): Set<Path> {
    val cacheDate = cacheDate
    val now = Clock.System.now()
    if (cachedApps == null || cacheDate == null || cacheDate + CACHE_DURATION < now) {
      cachedApps = withContext(Dispatchers.IO) { getMacOSAllApplications() }
      this.cacheDate = now
    }
    return cachedApps!!
  }
}
