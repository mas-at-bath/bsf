/**
 * Constant Class
 * 
 *      Created by JeeHang Lee
 *      A few constants used for the serialisation of DataReading class .
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace csSensorFramework
{
    public class Constant
    {
        // for RDF
        public static string XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        public static string MESSAGE_PREFIX = "edu.bath.sensorframework:";
        public static string COMMAND_SUBSCRIBE = "SUBSCRIBE";
        public static string COMMAND_UNSUBSCRIBE = "UNSUBSCRIBE";
        public static string COMMAND_DATA = "DATA";
        public static string BEGIN = "BEGIN";

        public static string sHAS_HANDLE = "http://bath.edu/sensors/predicates#hasHandle";
        public static string sTAKEN_BY = "http://bath.edu/sensors/predicates#takenBy";
        public static string sTAKEN_AT = "http://bath.edu/sensors/predicates#takenAt";
        public static string sPROVIDES = "http://bath.edu/sensors/predicates#provides";
        public static string sISDATAREADING = "http://bath.edu/sensors/predicates#isDataReading";
        public static string sISA = "http://bath.edu/sensors/predicates#isA";
        public static string sHASVALUE = "http://bath.edu/sensors/predicates#hasValue";
        public static string sPHYSICALLOCATION = "http://bath.edu/sensors/predicates#locatedAt";

        // for JSON 
        public static string JSON = "JSON";
        public static string XMLNS_JSON = "http://www.json.org/temp-ns#";
    }
}
