Kõnele
======

Kõnele service is an Android app that offers a speech-to-text service to other apps, in particular to Kõnele.
It implements [SpeechRecognizer](http://developer.android.com/reference/android/speech/SpeechRecognizer.html),
backed by an open source speech recognition server software - <https://github.com/alumae/kaldi-gstreamer-server>.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=ee.ioc.phon.android.k6neleservice)

Building the APK from source
----------------------------

Clone the source code including the `speechutils` submodule:

    git clone --recursive git@github.com:Kaljurand/K6nele-service.git


Point to the Android SDK directory by setting the environment variable
`ANDROID_HOME`, e.g.

    ANDROID_HOME=${HOME}/myapps/android-sdk/

Create the file `gradle.properties` containing the lines:

    org.gradle.jvmargs=-Xmx1536m
    org.gradle.parallel=true
    android.enableD8=true
    android.useAndroidX=true
    android.enableJetifier=true
    # Using the default ("false") for now because "true" actually makes the APK bigger for some reason
    # android.enableR8.fullMode=true

Build the app

    ./gradlew assemble


If you have access to a release keystore then

  - point to its location by setting the environment variable `KEYSTORE`
  - set `KEY_ALIAS` to the key alias
  - add these lines to `gradle.properties`:

        storePassword=<password1>
        keyPassword=<password2>


The (signed and unsigned) APKs will be generated into `app/build/outputs/apk/`.
