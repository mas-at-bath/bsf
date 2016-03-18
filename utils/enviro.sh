#!/bin/sh

sudo /home/pi/bsf/pythonFramework/BSFSensors.py | awk '{print $1 $2}' > /tmp/weather.txt
sudo /home/pi/bsf/pythonFramework/K-30.py >> /tmp/weather.txt
	      
$XYMON $XYMSRV "status $MACHINE.weather green `date`
`grep Temperature /tmp/weather.txt`
`grep Humidity /tmp/weather.txt`
`grep Pressure /tmp/weather.txt`
"
$XYMON $XYMSRV "status $MACHINE.gas green `date`
`grep NO /tmp/weather.txt`
`grep CO /tmp/weather.txt | grep -v CO2`
`grep CO2 /tmp/weather.txt`
"
$XYMON $XYMSRV "status $MACHINE.environment green `date`
`grep Light /tmp/weather.txt`
`grep Sound /tmp/weather.txt`
"

exit 0
