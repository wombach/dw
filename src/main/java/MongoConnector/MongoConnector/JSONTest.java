package MongoConnector.MongoConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class JSONTest {

	public static String writeJSONtoXML(JSONObject jobj){
//		File fXmlFile = new File(filename);
		String xml = XML.toString(jobj);
		return xml;
	}

	public static JSONObject readXMLtoJSON(String xml){
		JSONObject ret = null;
		ret = XML.toJSONObject(xml);
		return ret;
	}
	
	public static void main(String[] args) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
					"<model xmlns=\"http://www.opengroup.org/xsd/archimate\" identifier=\"id-551cc557\">\n"+
				   "<test /></model>";
		JSONObject obj = readXMLtoJSON(xml);
		System.out.println(obj.toString(3));
		String xml2 = writeJSONtoXML(obj);
		System.out.println(xml2);
	}

}
