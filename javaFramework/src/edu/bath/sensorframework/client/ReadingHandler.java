package edu.bath.sensorframework.client;

/**
 * Interface for handlers which deal with incoming readings.
 * @author adan
 *
 */
public interface ReadingHandler {
	/**
	 * Handles an incoming reading.
	 * @param node Node name this reading came from.
	 * @param rdf RDF data for reading.
	 * @see edu.bath.sensorframework.DataReading#fromRDF(String)
	 */
	public void handleIncomingReading(String node, String rdf);
}
