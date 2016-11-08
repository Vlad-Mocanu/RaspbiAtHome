# RaspbiAtHome
Android front-end for data received from remote sensors via REST calls.

## Description
The purpose is to graphically represent data sampled from remote sensors. This application does not do the actual recording of the data, and the device which runs the application is not physically connected to the sensors.

The application is tailored to work with https://github.com/Vlad-Mocanu/raspberry_sensors. The sampling of data is handled by **raspberry_sensors**. To make the sampled data available for **RaspbiAtHome** a REST web service must be provided (this is also handled by **raspberry_sensors**).

## Software
The development is done in Android Studio

## Releases
Given the fact that there are two repositories which can work together **RaspiAtHome** and **raspberry_sensors**, the release tags will have the same names for both repositories (ex: **DEV_xxx** or **REL_xxx**). When using the release tags for both repositories inter-compatibility between the sensor sampling + REST and graphical part is assured. 
