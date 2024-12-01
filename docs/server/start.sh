#!/bin/bash
source ~/myapps/whisper-fastapi/.venv/bin/activate
export LD_LIBRARY_PATH=`python3 -c 'import os; import nvidia.cublas.lib; import nvidia.cudnn.lib; print(os.path.dirname(nvidia.cublas.lib.__file__) + ":" + os.path.dirname(nvidia.cudnn.lib.__file__))'`
whisper=~/myapps/whisper-fastapi/whisper_fastapi.py
python $whisper --host 0.0.0.0 --port 3001 --model whisper-medium-et.ct2
#python $whisper --host 0.0.0.0 --port 3001 --model large-v3 &
