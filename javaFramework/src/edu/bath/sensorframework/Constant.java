package edu.bath.sensorframework;

/**
 * A few constants used for serialising DataReadings as RDF.
 * @author adan
 *
 */
public class Constant {
	public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static final String MESSAGE_PREFIX = "edu.bath.sensorframework:";
	public static final String COMMAND_SUBSCRIBE = "SUBSCRIBE";
	public static final String COMMAND_UNSUBSCRIBE = "UNSUBSCRIBE";
	public static final String COMMAND_DATA = "DATA";
	public static final String BEGIN = "BEGIN";
	
	public static final String sHAS_HANDLE = "http://bath.edu/sensors/predicates#hasHandle";
	public static final String sTAKEN_BY = "http://bath.edu/sensors/predicates#takenBy";
	public static final String sTAKEN_AT = "http://bath.edu/sensors/predicates#takenAt";
	public static final String sPROVIDES = "http://bath.edu/sensors/predicates#provides";
	public static final String sISDATAREADING = "http://bath.edu/sensors/predicates#isDataReading";
	public static final String sISA = "http://bath.edu/sensors/predicates#isA";
	public static final String sHASVALUE = "http://bath.edu/sensors/predicates#hasValue";
	public static final String sPHYSICALLOCATION = "http://bath.edu/sensors/predicates#locatedAt";
}
