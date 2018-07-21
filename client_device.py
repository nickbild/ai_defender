####
# Nick Bild
# nick.bild@gmail.com
# 2018-03-06
# Sample audio and detect gunshot sounds.
# This is intended to run on a network of
# dedicated hardware devices that are distributed
# throughout a location to be monitored.
# It can run on a PC with a microphone to simulate
# operation on a dedicated device.
# Make sure to set the "DEV_IDX" parameter to the
# appropriate device index of your microphone
# (commonly "0").
# Also be sure to set the server IP/port for your server
# instance.
# Give each client a unique CLIENT_ID and INSTITUTION_ID.
# To run:
# python client_device.py 2> /dev/null
# It will continue to run until stopped (e.g. CTRL-C).
####

import pyaudio
import wave
import tensorflow as tf
import pylab
import os
import numpy as np
import matplotlib.pyplot as plt
import socket

import keras
from keras.models import load_model
from keras.preprocessing import image

####
# Global variables.
####

# Audio recording parameters.
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 48000
CHUNK = 1024
RECORD_SECONDS = 2
WAVE_OUTPUT_FILENAME = "clip.wav"
SPECTROGRAM_OUTPUT_FILENAME = "clip_spectrogram.png"
DEV_IDX = 2

# Network parameters
INSTITUTION_ID = "UNIVERSITY-1705"
CLIENT_ID = "ROOM243"
SERVER_IP = "XXX.XXX.XXX.XXX"
SERVER_PORT = 8080

####
# Functions.
####

# Record a WAV from specified input device.
def recordClip(): 
	audio = pyaudio.PyAudio()

	# Uncomment line below to determine which device index should be used.
	# print audio.get_device_info_by_index(2)

	# Start recording.
	stream = audio.open(format=FORMAT, channels=CHANNELS,
        	rate=RATE, input=True,
        	frames_per_buffer=CHUNK,
		input_device_index=DEV_IDX)

	# Prepare data for WAV output.
	frames = [] 
	for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
		data = stream.read(CHUNK)
		frames.append(data)
 
	# Stop recording.
	stream.stop_stream()
	stream.close()
	audio.terminate()

	# Write WAV.
	waveFile = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
	waveFile.setnchannels(CHANNELS)
	waveFile.setsampwidth(audio.get_sample_size(FORMAT))
	waveFile.setframerate(RATE)
	waveFile.writeframes(b''.join(frames))
	waveFile.close()

# Generate graph of WAV signal.
def graph_freq(wav_file):
	spf = wave.open(wav_file, 'r')
	signal = spf.readframes(-1)
	signal = np.fromstring(signal, 'Int16')
	plt.axis((0,94000,-7000,7000))
	plt.figure(1)
	plt.plot(signal)
	pylab.savefig(SPECTROGRAM_OUTPUT_FILENAME)
	plt.gcf().clear() # Clear plot before next call.
	spf.close()

####
# Main program logic.
####

# Load the CNN model.
model = load_model('alert.h5')

# Infinite loop.
while 1==1:
	# Record a WAV audio clip.
	recordClip();

	# Generate spectrogram of WAV.
	graph_freq(WAVE_OUTPUT_FILENAME)

	# Resize, convert to black and white, and crop out axes/border.
	os.system("convert " + SPECTROGRAM_OUTPUT_FILENAME + " -resize 600x200! -type Grayscale -crop 454x151+80+22 +repage " + SPECTROGRAM_OUTPUT_FILENAME)

	# Evaluate data against model.
	img = image.load_img(SPECTROGRAM_OUTPUT_FILENAME, target_size=(151, 454))
	x = image.img_to_array(img)
	x = np.expand_dims(x, axis=0)
	image1 = np.vstack([x])
	classes = model.predict_classes(image1, batch_size=32)

	# Interpret model output.
	# Gun = 0; Ambient = 1
	if classes [0][0] == 0:
		print "Shots fired!"
		# Alert the remote server of this event.
		client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		client_socket.connect((SERVER_IP, SERVER_PORT))
		client_socket.send("ALERT " + CLIENT_ID + " " + INSTITUTION_ID)
		client_socket.close()
	elif classes [0][0] == 1:
		print "Nothing detected."

