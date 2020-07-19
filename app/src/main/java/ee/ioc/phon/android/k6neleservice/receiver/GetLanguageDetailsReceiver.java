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

package ee.ioc.phon.android.k6neleservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

import ee.ioc.phon.android.k6neleservice.R;
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;

public class GetLanguageDetailsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle resultExtras = getResultExtras(true);

        // TODO: not sure how we are supposed to behave in the case, where
        // another recognizer has already responded to the broadcast and filled
        // in its values.
        // if (!resultExtras.isEmpty()) { ... }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String locales = PreferenceUtils.getPrefString(prefs, context.getResources(), R.string.keyLocales, R.string.defaultLocales).trim();

        if (!locales.isEmpty()) {
            ArrayList<String> langs = new ArrayList<>(Arrays.asList(locales.split(",\\s*")));
            if (!langs.isEmpty()) {
                Bundle extras = new Bundle();
                extras.putString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langs.get(0));
                extras.putStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, langs);
                setResultExtras(extras);
            }
        }
    }
}