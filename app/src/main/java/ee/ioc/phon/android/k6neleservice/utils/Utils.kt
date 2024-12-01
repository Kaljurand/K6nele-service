/*
 * Copyright 2011-2021, Institute of Cybernetics at Tallinn University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.ioc.phon.android.k6neleservice.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import ee.ioc.phon.android.k6neleservice.Log.e

/**
 * Some useful static methods.
 *
 * @author Kaarel Kaljurand
 */
object Utils {
    @JvmStatic
    fun getVersionName(c: Context): String {
        val info = getPackageInfo(c) ?: return "?.?.?"
        val versionName = info.versionName ?: return "?.?.?"
        return versionName
    }

    private fun getPackageInfo(c: Context): PackageInfo? {
        val manager = c.packageManager
        try {
            return manager.getPackageInfo(c.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e("Couldn't find package information in PackageManager: $e")
        }
        return null
    }

    @JvmStatic
    fun makeUserAgentComment(tag: String, versionName: String, caller: String): String {
        return "${tag}/${versionName}; ${Build.MANUFACTURER}/${Build.DEVICE}/${Build.DISPLAY}; $caller"
    }
}