package ee.ioc.phon.android.k6neleservice.activity;

/*
 * Copyright 2011-2020, Institute of Cybernetics at Tallinn University of Technology
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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import ee.ioc.phon.android.k6neleservice.R;
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;

public class PreferencesRecognitionServiceWs extends AppCompatActivity {

    private static final int ACTIVITY_SELECT_SERVER_URL = 1;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private SettingsFragment mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettings)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        ActivityCompat.requestPermissions(PreferencesRecognitionServiceWs.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ACTIVITY_SELECT_SERVER_URL:
                Uri serverUri = data.getData();
                if (serverUri == null) {
                    toast(getString(R.string.errorFailedGetServerUrl));
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    PreferenceUtils.putPrefString(prefs, getResources(), R.string.keyWsServer, serverUri.toString());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSettings.setSummary(R.string.keyAppInfo, getString(R.string.summaryAppInfo));
                } else {
                    // Permission not granted: explain the consequences and offer a link to the app settings
                    mSettings.setSummary(R.string.keyAppInfo, getString(R.string.summaryAppInfo) + "\n\n" + getString(R.string.promptPermissionRationale));
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_header, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAbout:
                Intent searchIntent = new Intent(this, AboutActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.menuHelp:
                Intent view = new Intent(Intent.ACTION_VIEW);
                view.setData(Uri.parse(getString(R.string.urlDoc)));
                startActivity(view);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_server_ws, rootKey);
        }

        @Override
        public void onStart() {
            super.onStart();
            setSummaries(getPreferenceScreen().getSharedPreferences(), getResources());
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            Preference service = findPreference(getString(R.string.keyWsServer));
            service.setOnPreferenceClickListener(preference -> {
                getActivity().startActivityForResult(preference.getIntent(), ACTIVITY_SELECT_SERVER_URL);
                return true;
            });
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference etp = (EditTextPreference) pref;
                pref.setSummary(etp.getText());
            }
        }

        public void setSummary(int key, String text) {
            final Preference pref = findPreference(getString(key));
            if (pref != null) {
                pref.setSummary(text);
            }
        }

        private void setSummaries(SharedPreferences prefs, Resources res) {
            int key = R.string.keyWsServer;
            Preference pref = findPreference(getString(key));
            if (pref != null) {
                final String urlSpeech = PreferenceUtils.getPrefString(prefs, res, key, R.string.defaultWsServer);
                pref.setSummary(String.format(getString(R.string.summaryWsServer), urlSpeech));
            }
            key = R.string.keyLocales;
            pref = findPreference(getString(key));
            if (pref != null) {
                pref.setSummary(PreferenceUtils.getPrefString(prefs, res, key, R.string.defaultLocales));
            }
        }
    }
}