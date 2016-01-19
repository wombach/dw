package MongoConnector.MongoConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bson.BSONObject;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;

public class Parser {
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;

	public Parser(){
	}

	public void readXMLtoJSON(String filename){
		File fXmlFile = new File(filename);
		try {
			BufferedReader reader = new BufferedReader( new FileReader (filename));
			String         line = null;
			StringBuilder  stringBuilder = new StringBuilder();
			String         ls = System.getProperty("line.separator");

			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append( line );
				stringBuilder.append( ls );
			}
			JSONObject xmlJSONObj = XML.toJSONObject(stringBuilder.toString());
			String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
			JSONObject obj = xmlJSONObj.getJSONObject("model");
			JSONObject els =  obj.getJSONObject("elements");
			JSONArray l = els.getJSONArray("element");
//			JSONObject obje = new JSONObject()
			els.remove("element");
			String s = insertDocument(l.getJSONObject(1)).toString();
			System.out.println(s);
			System.out.println(jsonPrettyPrintString);
		} catch (JSONException je) {
			System.out.println(je.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document insertDocument(JSONObject obj){
		Document doc = new Document("name", "node")
				.append("type", "archimate")
				.append("start_date", System.currentTimeMillis())
				.append("end_date", -1)
				.append("raw", new Document("element", (BSONObject)com.mongodb.util.JSON.parse(obj.toString())));
//		MongoCollection<Document> collection = getCollection();
//		System.out.println(collection.count());
//		collection.insertOne(doc);
//		System.out.println(collection.count());
		return doc;
	}

	public void convertObject(){
//		DBObject dbObj = ... ;
//		String json = JSON.serialize( dbObj );
//		DBObject bson = ( DBObject ) JSON.parse( json );
	}

	public void readXML(String filename) throws ParserConfigurationException, SAXException, IOException{
//		File fXmlFile = new File(filename);
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		Document doc = dBuilder.parse(fXmlFile);
//
//		//optional, but recommended
//		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
//		doc.getDocumentElement().normalize();
//
//		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
//
//		System.out.println("schema: "+doc.getDocumentElement().getAttribute("xmlns"));
//		NamedNodeMap l = doc.getDocumentElement().getAttributes();
//
//		System.out.println("----------------------------");
//
//		for (int i=0;i<l.getLength();i++) {
//			System.out.println("   Attribute: " + l.item(i).getNodeName()+" - "+l.item(i).getNodeValue());
//		}
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		Parser p = new Parser();
//		p.readXML("OTK Sample.xml");
		p.readXMLtoJSON("OTK Sample.xml");
	}


}
