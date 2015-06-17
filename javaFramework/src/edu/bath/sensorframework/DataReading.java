package edu.bath.sensorframework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * How data readings are stored and passed around the system.
 * 
 * @author adan
 *
 */
public class DataReading {
	/**
	 * RDF triple (fairly self explanatory).
	 * 
	 * Supported object types are: Integer, Long, Byte, Boolean, Double, Float, 
	 * Short, String and URI (as a String).
	 * 
	 * @author adan
	 *
	 */
	public class Value {
		public final String predicate;
		public final String subject;
		public final Serializable object;
		public final boolean objectIsURI;
		
		public Value(String subject, String predicate, Serializable object, boolean objectIsURI) {
			this.predicate = predicate;
			this.subject = subject;
			this.object = object;
			this.objectIsURI = objectIsURI;//TODO: change this to a java URI type
		}
	}
	
	private String takenBy;
	private String locatedAt;
	private long timestamp;
	private String type;
	private List<Value> dataValues = new ArrayList<DataReading.Value>(3);
	private String readingURI = null; // only generated when you first serialise to RDF
	
	/**
	 * Creates a DataReading.
	 */
	public DataReading() {}
	
	/**
	 * Creates a DataReading with default values.
	 * @param takenBy URI of sensor this was taken by.
	 * @param locatedAt URI of location the sensor is in.
	 * @param timestamp Time the reading was taken.
	 */
	public DataReading(String takenBy, String locatedAt, long timestamp) {
		this.takenBy = takenBy;
		this.timestamp = timestamp;
		this.locatedAt = locatedAt;
	}
	
	/**
	 * Adds an RDF triple to this DataReading.
	 * @param val RDF Triple.
	 */
	public void addDataValue(Value val) { dataValues.add(val); }
	/**
	 * Adds an RDF triple to this DataReading.
	 * @param subject RDF subject.
	 * @param predicate RDF predicate.
	 * @param object RDF object (supported types detailed in DataReading.Value).
	 * @param objectIsURI Whether the object represents a URI if it is a String, or whether it is really a string literal.
	 */
	public void addDataValue(String subject, String predicate, Serializable object, boolean objectIsURI) {
		dataValues.add(new Value(subject, predicate, object, objectIsURI));
	}
	
	/**
	 * Fetch all RDF triples in the reading.
	 * @return
	 */
	public List<Value> getDataValues() { return this.dataValues; } // TODO: REMOVE THIS! MAKE SAFER ACCESSOR METHODS
	/**
	 * Finds a particular RDF triple using a simple query. Fields provided are 
	 * matched, any field given as null is treated as 'any value'.
	 * @param subject Subject to search for, or null.
	 * @param predicate Predicate to search for, or null.
	 * @param object Object to search for, or null;
	 * @return List of matching RDF triples (which could be of 0 length).
	 */
	public List<Value> findValues(String subject, String predicate, Serializable object) {
		List<Value> filteredValues = new ArrayList<Value>(dataValues.size());
		filteredValues.addAll(dataValues);
		
		// Filter by subject
		if(subject != null) {
			Iterator<Value> it = filteredValues.iterator();
			while(it.hasNext()) {
				Value val = (Value)it.next();
				if(!val.subject.equals(subject))
					it.remove();
			}
		}
		
		// Filter by predicate
		if(predicate != null) {
			Iterator<Value> it = filteredValues.iterator();
			while(it.hasNext()) {
				Value val = (Value)it.next();
				if(!val.predicate.equals(predicate))
					it.remove();
			}
		}
		
		// Filter by object
		if(object != null) {
			Iterator<Value> it = filteredValues.iterator();
			while(it.hasNext()) {
				Value val = (Value)it.next();
				if(!val.object.equals(object))
					it.remove();
			}
		}
		
		return filteredValues;
	}
	/**
	 * Performs a search as above, but only returns the first result.
	 * @param subject Subject to search for, or null.
	 * @param predicate Predicate to search for, or null.
	 * @param object Object to search for, or null;
	 * @return RDF triple if at least one is found, or null.
	 * @see DataReading#findValues(String, String, Serializable)
	 */
	public Value findFirstValue(String subject, String predicate, Serializable object) {
		List<Value> filteredValues = findValues(subject, predicate, object);
		if(filteredValues.size() >= 1)
			return filteredValues.get(0);
		else
			return null;
	}
	
	/**
	 * Fetch the URI of the sensor which took this reading.
	 * @return Sensor URI.
	 */
	public String getTakenBy() { return this.takenBy; }
	/**
	 * Fetch the time at which this reading was taken.
	 * @return UNIX timestamp.
	 */
	public long getTimestamp() { return this.timestamp; }
	/**
	 * Fetch the URI of the type of sensor which took this reading.
	 * @return Type URI.
	 */
	public String getType() { return this.type; }
	/**
	 * Fetch the URI of the location at which this sensor is positioned.
	 * @return URI of sensor location.
	 */
	public String getLocatedAt() { return this.locatedAt; }
	/**
	 * Fetch the URI uniquely identifying this reading.
	 * @return URI of this reading.
	 */
	public String getReadingURI() { return this.readingURI; }
	
	public void setTakenBy(String takenBy) { this.takenBy = takenBy; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	public void setType(String type) { this.type = type; }
	public void setLocation(String locatedAt) { this.locatedAt = locatedAt; }
	
	private String generateReadingURI(String agentID) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		String toHash = agentID +System.currentTimeMillis();
		
		BigInteger i = new BigInteger(1,md.digest(toHash.getBytes()));
		String uri = "http://bath.edu/sensors/readings/"+String.format("%1$032X", i);
		return uri;
	}
	
	/**
	 * Build a DataReading object from serialised RDF data.
	 * @param rdf Data to do-serialise.
	 * @return An equivalent DataReading object.
	 * @throws Exception
	 */
	public static DataReading fromRDF(String rdf) throws Exception {
		DataReading dr = new DataReading();
		
		Model model = null;
		try {
			model = ModelFactory.createDefaultModel();
		} catch(Throwable th) {
			System.out.println("Caught throwable: "+th.getMessage());
		}
		model.read(new ByteArrayInputStream(rdf.getBytes()), null);
		
		// Read out the data producer information
		StmtIterator iter = model.listStatements(null, model.getProperty(Constant.sISDATAREADING), (String)null);
		Statement s = null;
		while(iter.hasNext()) {
			s = iter.nextStatement();
		    
			dr.setTakenBy(s.getObject().toString());
			
//			String stripped = strVal.substring(0, strVal.length()-handle.length()-1);
//			String location = stripped.substring(stripped.lastIndexOf('/')+1);
//			dr.setLocation(location);
			
			dr.readingURI = s.getSubject().toString();
		}
	    model.remove(s);
	    
		// Read out the timestamp
		iter = model.listStatements(null, model.getProperty(Constant.sTAKEN_AT), (String)null);
		while(iter.hasNext()) {
			s = iter.nextStatement();
			
			if(s.getObject().canAs(Literal.class)) {
				Literal l = s.getObject().as(Literal.class);
				dr.setTimestamp(l.getLong());
			} else {
			}
		}
		model.remove(s);
		
		// Read out type
		iter = model.listStatements(null, RDF.type, (String)null);
		while(iter.hasNext()) {
			s = iter.nextStatement();

			if(s.getObject().isURIResource()) {
				dr.setType(s.getObject().asNode().getURI());
			} else {
			}
		}
		model.remove(s);
		
		// Read out location
		iter = model.listStatements(null, model.getProperty(Constant.sPHYSICALLOCATION), (String)null);
		while(iter.hasNext()) {
			s = iter.nextStatement();

			if(s.getObject().isURIResource()) {
				dr.setLocation(s.getObject().asNode().getURI());
			} else {
			}
		}
		model.remove(s);
		
		// Extract all others
		iter = model.listStatements();
		while(iter.hasNext()) {
			s = iter.nextStatement();
			
			Serializable object = null;
			boolean objIsURI = false;

			if(s.getObject().isURIResource()) {
				// This is a URI
				object = s.getObject().asNode().getURI();
				objIsURI = true;
			} else if(s.getObject().isLiteral()) {
				Literal o = s.getObject().as(Literal.class);
				String typeURI = o.getDatatype().getURI();
				if(typeURI.equals("http://www.w3.org/2001/XMLSchema#int"))
					object = new Integer(o.getInt());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#long"))
					object = new Long(o.getLong());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#byte"))
					object = new Byte(o.getByte());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#boolean"))
					object = new Boolean(o.getBoolean());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#double"))
					object = new Double(o.getDouble());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#float"))
					object = new Float(o.getFloat());
				else if(typeURI.equals("http://www.w3.org/2001/XMLSchema#short"))
					object = new Short(o.getShort());
				else
					object = o.getString(); // also captures #string
			} else {
				object = s.getObject().toString();
			}
			
			dr.dataValues.add(dr.new Value(s.getSubject().getURI(), 
					s.getPredicate().getURI(), object, objIsURI));
		}
		
		return dr;
	}
	
	/**
	 * Serialises this DataReading object as RDF.
	 * @return RDF representation of this DataReading object.
	 * @throws UnsupportedEncodingException
	 */
	public String toRDF() throws UnsupportedEncodingException {

		long nanoToMili=1000000;
		long preTime = System.nanoTime();

		// Set up a repository in memory
		Model model = ModelFactory.createDefaultModel();
		
		// Serialise this object as RDF
		Resource objectIdentifier = (readingURI!=null?
				model.createResource(readingURI):
				model.createResource(readingURI=generateReadingURI(locatedAt+takenBy)));
		
		// Base properties
		model.add(objectIdentifier, model.createProperty(Constant.sISDATAREADING), 
				model.createResource(takenBy));
		model.add(objectIdentifier, model.createProperty(Constant.sTAKEN_AT), 
				model.createTypedLiteral(timestamp));
		model.add(objectIdentifier, RDF.type, 
				model.createResource(type));
		model.add(objectIdentifier, model.createProperty(Constant.sPHYSICALLOCATION), 
				model.createResource(locatedAt));
//		System.out.println("Located at: "+locatedAt);
		
		// Now output all the data values
		for(Value val : dataValues) {
			Resource subject = (val.subject==null?objectIdentifier:model.createResource(val.subject));
			Property predicate = model.createProperty(val.predicate);
			
			if(val.objectIsURI)
				model.add(subject, predicate, 
						model.createResource(val.object.toString()));
			else {
				if(val.object instanceof String)
					model.add(subject, predicate,
							model.createTypedLiteral((String)val.object));
				else if(val.object instanceof Boolean)
					model.add(subject, predicate,
							model.createTypedLiteral((Boolean)val.object));
				else if(val.object instanceof Byte)
					model.add(subject, predicate,
							model.createTypedLiteral((Byte)val.object));
				else if(val.object instanceof Double)
					model.add(subject, predicate,
							model.createTypedLiteral((Double)val.object));
				else if(val.object instanceof Float)
					model.add(subject, predicate,
							model.createTypedLiteral((Float)val.object));
				else if(val.object instanceof Integer)
					model.add(subject, predicate,
							model.createTypedLiteral((Integer)val.object));
				else if(val.object instanceof Long)
					model.add(subject, predicate,
							model.createTypedLiteral((Long)val.object));
				else if(val.object instanceof Short)
					model.add(subject, predicate,
							model.createTypedLiteral((Short)val.object));
				else
					model.add(subject, predicate,
							model.createTypedLiteral(val.object.toString()));
			}
		}
		
		//System.out.println("1 took " + ((System.nanoTime()-preTime)/nanoToMili));
		// Write the RDF to a string buffer as XML
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//System.out.println("2 took " + ((System.nanoTime()-preTime)/nanoToMili));
		model.write(bos);
		//System.out.println("3 took " + ((System.nanoTime()-preTime)/nanoToMili));
		String rdfOutput = bos.toString(Charset.defaultCharset().name());
//		System.out.println("RDF: "+rdfOutput);
		//System.out.println("toRDF conversion took " + ((System.nanoTime()-preTime)/nanoToMili));
		return rdfOutput;
	}
}
