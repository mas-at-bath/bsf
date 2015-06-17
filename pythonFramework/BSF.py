#!/usr/bin/python
import sys
import paho.mqtt.publish as publish
import hashlib
import time
import rdflib
from rdflib import Graph, Literal, BNode, Namespace, RDF, URIRef
from xml.sax.saxutils import escape

def current_milli_time():
   ##problem here newTime = lambda: 
    newTime = int(round(time.time() * 1000))
    return newTime

class Constant():
    XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    MESSAGE_PREFIX = "edu.bath.sensorframework:"
    COMMAND_SUBSCRIBE = "SUBSCRIBE"
    COMMAND_UNSUBSCRIBE = "UNSUBSCRIBE"
    COMMAND_DATA = "DATA"
    BEGIN = "BEGIN"
    sHAS_HANDLE = "http://bath.edu/sensors/predicates#hasHandle"
    sTAKEN_BY = "http://bath.edu/sensors/predicates#takenBy"
    sTAKEN_AT = "http://bath.edu/sensors/predicates#takenAt"
    sPROVIDES = "http://bath.edu/sensors/predicates#provides"
    sISDATAREADING = "http://bath.edu/sensors/predicates#isDataReading"
    sISA = "http://bath.edu/sensors/predicates#isA"
    sHASVALUE = "http://bath.edu/sensors/predicates#hasValue"
    sPHYSICALLOCATION = "http://bath.edu/sensors/predicates#locatedAt"


class Value(object):
       predicate = str()
       subject = str()
       #object_ = Serializable()
       object_ = str()
       objectIsURI = bool()

       def __init__(self, subject, predicate, object_, objectIsURI):
            self.predicate = predicate
            self.subject = subject
            self.object_ = object_
            self.objectIsURI = objectIsURI

class DataReading:
    takenBy = str()
    locatedAt = str()
    timestamp = long()
    type_ = str()
    ##dataValues = ArrayList(3)
    dataValues = []
    readingURI = None

    #@overloaded
    #def __init__(self):

    #@__init__.register(object, str, str, long)
    def __init__(self, takenBy, locatedAt, timestamp):
        self.takenBy = takenBy
        self.timestamp = timestamp
        self.locatedAt = locatedAt

    #@overloaded
    #def addDataValue(self, val):
    #    """ generated source for method addDataValue """
    #    self.dataValues.add(val)

    #@addDataValue.register(object, str, str, Serializable, bool)
    def addDataValue(self, subject, predicate, object_, objectIsURI):
	newValue = Value(subject, predicate, object_, objectIsURI)
        self.dataValues.append(newValue)

    # public List<Value> getDataValues() 
    # public List<Value> findValues(String subject, String predicate, Serializable object) 
    # public Value findFirstValue(String subject, String predicate, Serializable object) 
    def getTakenBy(self):
        return self.takenBy

    def getTimestamp(self):
        return self.timestamp

    def getType(self):
        return self.type_

    def getLocatedAt(self):
        """ generated source for method getLocatedAt """
        return self.locatedAt

    def getReadingURI(self):
        return self.readingURI

    def setTakenBy(self, takenBy):
        self.takenBy = takenBy

    def setTimestamp(self, timestamp):
        self.timestamp = timestamp

    def setType(self, type_):
        self.type_ = type_

    def setLocation(self, locatedAt):
        self.locatedAt = locatedAt

    def generateReadingURI(self, agentID):
        """ generated source for method generateReadingURI """
        md = None
	toHash = ""+agentID + str(current_milli_time())
	hash_object = hashlib.md5(toHash.encode())
        #try:
        #    md = MessageDigest.getInstance("MD5")
        #except NoSuchAlgorithmException as e:
        #    e.printStackTrace()
        #toHash = agentID + System.currentTimeMillis()
        #i = BigInteger(1, md.digest(toHash.getBytes()))
        #uri = "http://bath.edu/sensors/readings/" + String.format("%1$032X", i)
	# get something like this http://bath.edu/sensors/readings/B7748FA1983B1B2741BB1E973A5B8EA7
	uri = "http://bath.edu/sensors/readings/" +hash_object.hexdigest().upper()
        return uri

    def toRDF(self):
	g = rdflib.Graph()
	if self.readingURI is None:
		readingURI=self.generateReadingURI(self.locatedAt + self.takenBy)
	aref = URIRef(readingURI)
	g.add((aref, URIRef(Constant.sISDATAREADING), Literal(self.takenBy)))
        g.add((aref, URIRef(Constant.sTAKEN_AT), Literal(self.timestamp)))
       # g.add((aref, URIRef("rdf:type"), Literal(self.type_)))
        g.add((aref, URIRef(Constant.sPHYSICALLOCATION), Literal(self.locatedAt)))

	for val in self.dataValues:
            # maybe extend back with other types sometime
		print "adding a value"
		g.add((aref, URIRef(val.predicate), Literal(val.object_)))

	#name = BNode()
	#model.add(objectIdentifier, model.createProperty(Constant.sISDATAREADING), model.createResource(self.takenBy))
        #model.add(objectIdentifier, model.createProperty(Constant.sTAKEN_AT), model.createTypedLiteral(self.timestamp))
        #model.add(objectIdentifier, RDF.type_, model.createResource(self.type_))
        #model.add(objectIdentifier, model.createProperty(Constant.sPHYSICALLOCATION), model.createResource(self.locatedAt))
	#graph.add((johnSmith, VCARD['N'], name))
	#graph.add((name, VCARD['Given'], Literal(givenName)))
	#return objectIdentifier    
	return g.serialize()   


#newReading = DataReading("http://127.0.0.1/PISensors", "http://127.0.0.1/PiSensors/Pi", current_milli_time())
#newReading.setTakenBy("http://127.0.0.1/components/houseSensors/piSensor1")
#newReading.addDataValue(None, "http://127.0.0.1/sensors/types#GasNO", 50.0, False)
#msgString = "<RDF>"+escape(newReading.toRDF())+"</RDF>"
#print msgString
#publish.single("homeSensor", msgString, hostname="192.168.0.8")
