import RPi.GPIO as GPIO
import dht11
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
from time import sleep
from datetime import date, datetime

# initialize GPIO
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.cleanup()

# AWS IoT certificate based connection
myMQTTClient = AWSIoTMQTTClient("123afhlss456")
myMQTTClient.configureEndpoint("a26y5ns1489swl.iot.us-west-2.amazonaws.com", 8883)
myMQTTClient.configureCredentials("/home/pi/deviceSDK/certs/CA.pem", "/home/pi/deviceSDK/certs/66c37113d5-private.pem.key", "/home/pi/deviceSDK/certs/66c37113d5-certificate.pem.crt")
myMQTTClient.configureOfflinePublishQueueing(-1)  # Infinite offline Publish queueing
myMQTTClient.configureDrainingFrequency(2)  # Draining: 2 Hz
myMQTTClient.configureConnectDisconnectTimeout(10)  # 10 sec
myMQTTClient.configureMQTTOperationTimeout(5)  # 5 sec

#connect and publish
print 'hellosss'
myMQTTClient.connect()
myMQTTClient.publish("thing01/info", "connected", 0)

#loop and publish sensor reading
with file('newfile.txt') as f:s = f.read()
print s
myMQTTClient.publish("thing01/data", s, 0) 
sleep(4)
print (".")
