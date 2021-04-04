/*
 * Copyright 2012-2020, Institute of Cybernetics at Tallinn University of Technology
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
package ee.ioc.phon.android.k6neleservice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.preference.PreferenceManager
import ee.ioc.phon.android.k6neleservice.R
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils

class GetLanguageDetailsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: not sure how we are supposed to behave in the case, where
        // another recognizer has already responded to the broadcast and filled
        // in its values.
        // val resultExtras = getResultExtras(true)
        // if (!resultExtras.isEmpty()) { ... }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val localesAsStr = PreferenceUtils.getPrefString(prefs, context.resources, R.string.keyLocales, R.string.defaultLocales);

        val langs = localesAsStr.split(",").map { it.trim() }.filter({ it.isNotEmpty() })
        if (!langs.isEmpty()) {
            val extras = Bundle()
            extras.putString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langs[0])
            extras.putStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, ArrayList(langs))
            setResultExtras(extras)
        }
    }
}