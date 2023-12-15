# Running your own Kõnele server

(Work in progress)

## Introduction

Here are some examples of how to set up a speech recognition server in your home network,
that you can use from Kõnele service. This involves

- setting up the server software (incl. the models),
- starting the server at a given port,
- configuring your computer to expose the port in your local network,
- setting the "Server URL" in Kõnele service settings to point to the running server,
  or setting the ``ee.ioc.phon.android.extra.SERVER_URL`` extra in the app that
  calls Kõnele service (e.g. Kõnele, or an app that calls Kõnele).

## Servers

### kaldi-gstreamer-server

Install https://hub.docker.com/r/alumae/konele/
and configure the Kõnele service "Server URL" to something like "ws://192.168.0.38:8080/client/ws/speech" (see below).

See also
http://kaljurand.github.io/K6nele/docs/et/user_guide.html#tuvastusserver-koduv%C3%B5rgus
(in Estonian).

### whisper-fastapi

The following was tested on a i7 laptop with 16 GB RAM and with the NVIDIA RTX 4050 GPU,
running Windows 11 (22H2) with WSL v2.
Storage needs: Python venv 5.7 GB; models in .cache/huggingface: 13 GB (2 x large model + 1 x medium).

In ``C:\Users\<username>\.wslconfig`` add ``networkingMode=mirrored``
(adding it in both the wsl2- and the experimental-sections did not seem to hurt):

```
[wsl2]
networkingMode=mirrored

[experimental]
networkingMode=mirrored
```

See more:

- https://github.com/microsoft/WSL/issues/4150 (read only the most recent comments)
- https://devblogs.microsoft.com/commandline/windows-subsystem-for-linux-september-2023-update/
- https://learn.microsoft.com/en-us/windows/wsl/wsl-config

In WSL, install Python (with venv support), git, etc.
Set up a Python venv and install CUDA as explained in
https://github.com/SYSTRAN/faster-whisper

```
$ pip install nvidia-cublas-cu11 nvidia-cudnn-cu11
```

Install https://github.com/heimoshuiyu/whisper-fastapi and its dependencies:

```
$ git clone https://github.com/heimoshuiyu/whisper-fastapi.git
$ cd whisper-fastapi
$ pip install -r requirements.txt
```

Optional: Install finetuned Estonian models from https://huggingface.co/TalTechNLP

```
$ pip install transformers[torch]
# Note: the model page on Hugging Face instructs to add "--copy_files tokenizer.json",
# but this results in an error because "tokenizer.json does not exist in model"
$ ct2-transformers-converter --model TalTechNLP/whisper-medium-et --output_dir whisper-medium-et.ct2 --copy_files tokenizer.json --quantization float16

# TODO: gets killed (16 GB is not enough?)
$ ct2-transformers-converter --model TalTechNLP/whisper-large-et --output_dir whisper-large-et.ct2 --copy_files tokenizer.json --quantization float16
```

Start the server(s):

```
$ export LD_LIBRARY_PATH=`python3 -c 'import os; import nvidia.cublas.lib; import nvidia.cudnn.lib; print(os.path.dirname(nvidia.cublas.lib.__file__) + ":" + os.path.dirname(nvidia.cudnn.lib.__file__))'`
$ whisper=pathto/to/whisper-fastapi/whisper_fastapi.py
$ python $whisper --host 0.0.0.0 --port 3000 --model whisper-medium-et.ct2 &
$ python $whisper --host 0.0.0.0 --port 3001 --model large-v3 &
```

Running multiple server might not work, depending on the available RAM.

Change the Windows firewall rules to allow e.g. ports "3000-3010".
Open "Windows Defender Firewall with Advanced Security on Local Computer", then "Inbound rules",
and "New Rule...", and add the desired ports.
See e.g. https://www.howtogeek.com/112564/how-to-create-advanced-firewall-rules-in-the-windows-firewall/

The Kõnele service "Server URL" will be: ``ws://10.0.0.160:3000/k6nele/ws?initial_prompt=1+2+3``

## Configuration in Kõnele service.

In order to use the running server from Kõnele service, specify its URL in the "Server URL" field. The server URL depends on its IP address in the local network.

To find out the home server's IP address, use the Unix command "ip addr", or visit the home router's configuration page (often at http://192.168.0.1).
An Android, use an app like Fing (https://www.fing.com/products/fing-app). Also, the Kõnele service "Server URL"
activity allows searching for devices in the same network, and to check if they are running the server. For example, a message "2 free slots" under the service address box means that the service is operational and currently allows a maximum of two simultaneous recognition sessions.

The server address can also be overridden with the EXTRA ``ee.ioc.phon.android.extra.SERVER_URL``, e.g. when Kõnele server is called via Kõnele
which in turn is launched by another app (e.g., Tasker, Android Debug Bridge, a custom application, or Kõnele rewrite rules).
