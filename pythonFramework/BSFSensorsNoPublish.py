#!/usr/bin/python

#code to read MCP analogue readings based on http://blog.ubidots.com/hands-on-airpi-kit-a-weather-station-using-raspberry-pi 

import sys
import argparse
import os
import serial
import time
import Adafruit_DHT
import RPi.GPIO as GPIO 			# Library for using the GPIO ports
from  math import log1p,exp,log10 	# Library for math functions. 
from xml.sax.saxutils import escape
import paho.mqtt.publish as publish
scriptpath = "./BSF.py"
sys.path.append(os.path.abspath(scriptpath))
import BSF
from BSF import DataReading
import Adafruit_BMP.BMP085 as BMP085

#maybe add a check that we're running as root?
allSensors=False
tempSensor=False
humiSensor=False
coSensor=False
no2Sensor=False
luxSensor=False
presSensor=False
noiseSensor=False

parser = argparse.ArgumentParser(description='Sensor parsing.')
parser.add_argument('-s','--sensors', help='Process which sensors',required=True)
args = parser.parse_args()

if args.sensors == 'all':
	allSensors=True
elif args.sensors == 'temp':
	tempSensor=True
elif args.sensors == 'humi':
	humiSensor=True
elif args.sensors == 'co':
	coSensor=True
elif args.sensors == 'no2':
	no2Sensor=True
elif args.sensors == 'lux':
	luxSensor=True
elif args.sensors == 'pressure':
	presSensor=True
elif args.sensors == 'noise':
	noiseSensor=True
else:
	print "couldnt understand sensor choice, should be all, temp, humi, co, no2, lux, pressure, noise"

# Set up the SPI interface pins. Through SPI we can connect to the ADC MCP3008

if allSensors or no2Sensor or luxSensor or coSensor:
	SPICLK = 18
	SPIMISO = 24
	SPIMOSI = 23
	SPICS = 25
	GPIO.setmode(GPIO.BCM) 				# Set up BCM as numbering system for inputs
	GPIO.setup(SPIMOSI, GPIO.OUT)		# Configure the SPI I/O
	GPIO.setup(SPIMISO, GPIO.IN)
	GPIO.setup(SPICLK, GPIO.OUT)
	GPIO.setup(SPICS, GPIO.OUT)
	GPIO.setwarnings(False)

# Setup Variables
light = 0 							# Save  value of the LDR
noise = 0 							# Save value of the Mic in
no2 = 0 							# Save value for Nitrogen dioxide level
co = 0 								# Save value for Carbon monoxide level
vin = 3.3  							# Voltage reference for the ADC    

#setup the K30 sensor
##ser = serial.Serial("/dev/ttyAMA0")
##ser.flushInput()
##time.sleep(1)


if allSensors or presSensor:
	#setup BMP80 sensor
	sensor = BMP085.BMP085()

# Function from Adafruit to read analog values

def readadc(adcnum, clockpin, mosipin, misopin, cspin):
        if ((adcnum > 7) or (adcnum < 0)):
                return -1
        GPIO.output(cspin, True)

        GPIO.output(clockpin, False)  		# start clock low
        GPIO.output(cspin, False)     		# bring CS low

        commandout = adcnum
        commandout |= 0x18  				# start bit + single-ended bit
        commandout <<= 3    				# we only need to send 5 bits here
        for i in range(5):
                if (commandout & 0x80):
                        GPIO.output(mosipin, True)
                else:
                        GPIO.output(mosipin, False)
                commandout <<= 1
                GPIO.output(clockpin, True)
                GPIO.output(clockpin, False)
        adcout = 0
        # read in one empty bit, one null bit and 10 ADC bits
        for i in range(12):
                GPIO.output(clockpin, True)
                GPIO.output(clockpin, False)
                adcout <<= 1
                if (GPIO.input(misopin)):
                        adcout |= 0x1
        GPIO.output(cspin, True)
        
        adcout >>= 1       					# first bit is 'null' so drop it
        return adcout



if allSensors or luxSensor:
	# Code to get light levels data
	light = readadc(0, SPICLK, SPIMOSI, SPIMISO, SPICS) # Read the analog pin where the LDR is connected
	light = float(light)/1023*vin						# Voltage value from ADC
	light = 10000/((vin/light)-1)						# Ohm value of the LDR, 10k is used as Pull up Resistor
	light = exp((log1p(light/1000)-4.125)/-0.6704)		# Linear aproximation from http://pi.gate.ac.uk/posts/2014/02/25/airpisensors/ to get Lux value
	print "Light=%f lux" % light
	

if allSensors or noiseSensor:
	# Code to get audio levels from  20 hz frequency
	signalMax = 0
	signalMin = 1024
	startMillis = int(round(time.time()*1000))			# Time in milliseconds

	while (int(round(time.time()*1000))-startMillis<50):    # Collect data for 20hz frequency, 20hz=1/50ms    
		noise = readadc(4, SPICLK, SPIMOSI, SPIMISO, SPICS) # Read the analog input where the mic is connected
		if (noise < 1024):
			if(noise > signalMax):
				signalMax = noise
			elif(noise < signalMin):
				signalMin = noise
	peakToPeak = signalMax - signalMin
	#print peakToPeak					# Peak to Peak value
	db = float((peakToPeak*vin*1000)/1023)			#Measure in mV
	#time.sleep(0.1)
	print "Sound=%f mV" % db

	
if allSensors or no2Sensor:
	# Code to read NO2 and CO concentrations
	no2 = readadc(2, SPICLK, SPIMOSI, SPIMISO, SPICS)	# Read the analog input for the nitrogen value	
	no2 = float(no2)/1023*vin								# Voltage value from the ADC
	no2 = ((10000*vin)/no2)-10000							# Ohm value of the no2 resistor, 10k  is used as pull down resistor 
	#no2 = float(no2/700)					#Reference value
	#time.sleep(0.1)
	print "NO=%f ohms" % no2

	
if allSensors or coSensor:
	co = readadc(3, SPICLK, SPIMOSI, SPIMISO, SPICS)		# Read the analog input for the carbon value
	co = float(co)/1023*vin 								# Voltage value from the ADC
	co = ((360000*vin)/co)-360000							# Ohm Value of the co resistor, 360k is used as pull down resistor
	#co = float(co/30000) 					#Reference value
	print "CO=%f ohms" % co


if allSensors or tempSensor or humiSensor:
	humidity, temperature = Adafruit_DHT.read_retry(22, 4)
	if humidity is not None and temperature is not None:
		if allSensors or humiSensor:
			print "Humidity=%f %%" % humidity
		if allSensors or tempSensor:
			print "Temperature=%f C " % temperature


if allSensors:
	##ser.write("\xFE\x44\x00\x08\x02\x9F\x25")
	##time.sleep(.01)
	##resp = ser.read(7)
	##high = ord(resp[3])
	##low = ord(resp[4])
	##if high is not None and low is not None:
	##	co2 = (high*256) + low
	print "CO2=%f ppm" % co2


if allSensors or presSensor:
	bmpTemp = sensor.read_temperature()
	bmpPressure = sensor.read_pressure()
	if bmpTemp is not None and bmpPressure is not None:
		print "Pressure=%f Pa" % bmpPressure
		#print "Temp=%f C" % bmpTemp

if allSensors or luxSensor or no2Sensor or coSensor:
	GPIO.cleanup()						# Reset the status of the GPIO pins
