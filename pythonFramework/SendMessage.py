#!/usr/bin/python
import sys
import os
from xml.sax.saxutils import escape
import paho.mqtt.publish as publish
scriptpath = "./BSF.py"
sys.path.append(os.path.abspath(scriptpath))
import BSF
from BSF import DataReading

if len(sys.argv) == 5:
	room = sys.argv[2]
	change = sys.argv[4]
else:
	print 'usage: ./SendMessage.py house ROOM lights ON|OFF|UP|DOWN'
	sys.exit(1)


newReading = DataReading("http://127.0.0.1/PISensors", "http://127.0.0.1/PiSensors/Pi", BSF.current_milli_time())
newReading.setTakenBy("http://127.0.0.1/components/houseSensors/voice1")
print room
print change
newReading.addDataValue(None, "http://127.0.0.1/sensors/types#room", room, False)
newReading.addDataValue(None, "http://127.0.0.1/sensors/types#change", change, False)
msgString = "<RDF>"+escape(newReading.toRDF())+"</RDF>"
publish.single("homeSensor", msgString, hostname="192.168.0.8")
