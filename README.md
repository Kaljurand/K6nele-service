Kõnele service
==============

Kõnele service is an Android app that offers a speech-to-text service to other apps, in particular to Kõnele (<https://github.com/Kaljurand/K6nele>).
It implements Android's [SpeechRecognizer](http://developer.android.com/reference/android/speech/SpeechRecognizer.html) interface to
an open source speech recognition server software <https://github.com/alumae/kaldi-gstreamer-server>.

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

In order to change your build environment create the file `gradle.properties`
at a location pointed to by the environment variable `GRADLE_USER_HOME`.
This will extend and override the definitions found in the `gradle.properties`
that is part of the release.

Build the app

    ./gradlew assemble

If you have access to a release keystore then add these lines to the extended `gradle.properties`:

    storeFilename=</path/to/store.jks>
    storePassword=<storePassword>
    keyAlias=<keyAlias>
    keyPassword=<keyPassword>

The (signed and unsigned) APKs will be generated into `app/build/outputs/apk/`.
