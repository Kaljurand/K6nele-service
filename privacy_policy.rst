Privacy policy
==============

Kõnele service performs the recording and transcribing of audio. The start of the
recording and transcribing depends on the caller of the service (e.g. when a
microphone button is pressed in its user interface).

The service itself can stop the recording after a longer pause in the input audio (depending on the
settings).
The service can be configured to signal the beginning and end of the recording with audio cues.

For the recording, Kõnele service requires the microphone permission.
For the transcribing, Kõnele service requires network access to a speech recognition server. The network connection is not encrypted. The connection is established when the recording is started and closed when the recording is stopped and the final transcription has been received.

Kõnele service has a single corresponding recognition server, whose web address is visible and changeable in the Kõnele service settings. The server runs independently of Kõnele service and is covered by its own privacy policy. The privacy policy of the default server is available at http://phon.ioc.ee. The default server is based on free and open source software (available at https://github.com/alumae/kaldi-gstreamer-server) allowing the user to install it in a local private network.

Apart from using a third-party speech recognition server as described above, Kõnele service does not collect nor share any user data. The source code of all Kõnele service components and their dependencies is open and available at https://github.com/Kaljurand/K6nele-service.
