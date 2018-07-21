####
# Nick Bild
# nick.bild@gmail.com
# 2018-03-08
# Build a convolutional neural network model
# and train it with spectrogram image data.
# Requires tensorflow and keras.
# To run:
# Make sure directory structure is set up:
# data/train/<CATEGORY1>/
# data/train/<CATEGORY2>/
# data/validation/<CATEGORY1>/
# data/validation/<CATEGORY2>/
#
# Then run:
# python build_model_and_train.py
####

import tensorflow as tf

import keras
from keras.models import Sequential
from keras.layers.convolutional import Conv2D
from keras.layers import MaxPooling2D, Flatten, Dense
from keras.preprocessing.image import ImageDataGenerator, array_to_img, img_to_array, load_img
from keras.layers.core import Activation, Dropout
from keras.optimizers import Adam

# Dimensions of input images (height, width, channels).
input_shape = (151, 454, 3)
num_classes = 2 # Number of output classes (binary in this case -- gunshot or not gunshot).

# Create a convolutional neural network model.
model = Sequential()
model.add(Conv2D(4, kernel_size=(5, 5), strides=(1, 1), activation='relu', input_shape=input_shape))
model.add(MaxPooling2D())
model.add(Conv2D(6, (5, 5), activation='relu'))
model.add(Dropout(0.2))
model.add(MaxPooling2D())
model.add(Flatten())
model.add(Dropout(0.2))
model.add(Dense(8, activation='relu'))
model.add(Dense(1, activation='sigmoid')) # This gives binary output in the final layer.

# Epochs to train model for.
EPOCHS = 100

# Compile the model.
model.compile(loss="binary_crossentropy", optimizer="adam", metrics=["accuracy"])

# Prepare the training and validation data.
batch_size = 16
train_datagen = ImageDataGenerator()
test_datagen = ImageDataGenerator()

train_generator = train_datagen.flow_from_directory(
        'data/train',
        target_size=(151, 454),
        batch_size=batch_size,
	shuffle=True,
        class_mode='binary')

validation_generator = test_datagen.flow_from_directory(
        'data/validation',
        target_size=(151, 454),
        batch_size=batch_size,
        class_mode='binary')

# Train the model.
model.fit_generator(
        train_generator,
        steps_per_epoch=150 // batch_size,
        epochs=EPOCHS,
        validation_data=validation_generator,
        validation_steps=200 // batch_size)

# Save the trained model.
model.save('alert.h5')

