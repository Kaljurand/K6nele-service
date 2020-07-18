/*
 * Copyright 2011-2015, Institute of Cybernetics at Tallinn University of Technology
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

package ee.ioc.phon.android.k6neleservice.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ee.ioc.phon.android.k6neleservice.Log;
import ee.ioc.phon.android.k6neleservice.R;

/**
 * <p>Some useful static methods.</p>
 *
 * @author Kaarel Kaljurand
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Creates a non-cancelable dialog with two buttons, both finish the activity,
     * one launches the given intent first.
     * TODO: note that we explicitly set the dialog style. This is because if the caller activity's style
     * is Theme.Translucent.NoTitleBar then the dialog is unstyled (maybe an Android bug?)
     */
    public static AlertDialog getLaunchIntentDialog(final AppCompatActivity activity, String msg, final Intent intent) {
        return new AlertDialog.Builder(activity, R.style.Theme_K6nele_Dialog)
                .setPositiveButton(activity.getString(R.string.buttonGoToSettings), (dialog, id) -> {
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton(activity.getString(R.string.buttonCancel), (dialog, id) -> {
                    dialog.cancel();
                    activity.finish();
                })
                .setMessage(msg)
                .setCancelable(false)
                .create();
    }

    public static String getVersionName(Context c) {
        PackageInfo info = getPackageInfo(c);
        if (info == null) {
            return "?.?.?";
        }
        return info.versionName;
    }


    private static PackageInfo getPackageInfo(Context c) {
        PackageManager manager = c.getPackageManager();
        try {
            return manager.getPackageInfo(c.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e("Couldn't find package information in PackageManager: " + e);
        }
        return null;
    }


    public static String chooseValue(String firstChoice, String secondChoice) {
        if (firstChoice == null) {
            return secondChoice;
        }
        return firstChoice;
    }


    public static String chooseValue(String firstChoice, String secondChoice, String thirdChoice) {
        String choice = chooseValue(firstChoice, secondChoice);
        if (choice == null) {
            return thirdChoice;
        }
        return choice;
    }


    public static String makeUserAgentComment(String tag, String versionName, String caller) {
        return tag + "/" + versionName + "; " +
                Build.MANUFACTURER + "/" +
                Build.DEVICE + "/" +
                Build.DISPLAY + "; " +
                caller;
    }

    public static <E> List<E> makeList(Iterable<E> iter) {
        List<E> list = new ArrayList<>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
}