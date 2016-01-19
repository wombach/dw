package MongoConnector.MongoConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bson.BSONObject;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoConnectorMain {
	
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
        "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";

    
	public MongoConnectorMain(){
	}

	public MongoCollection<Document> getCollection(){
		// or use a connection string
//		MongoClientURI connectionString = new MongoClientURI(); //("mongodb://127.0.0.1:27017,localhost:27018,localhost:27019");
		MongoClient mongoClient = new MongoClient();//(connectionString);
		MongoDatabase database = mongoClient.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("restaurants");
		return collection;
	}

	public void insertDocument(){
		Document doc = new Document("name", "MongoDB")
				.append("type", "database")
				.append("count", 1)
				.append("info", new Document("x", 203).append("y", 102));
		MongoCollection<Document> collection = getCollection();
		System.out.println(collection.count());
		collection.insertOne(doc);
		System.out.println(collection.count());
	}

	public void getAllDocuments(){
		MongoCollection<Document> collection = getCollection();
		MongoCursor<Document> cursor = collection.find().iterator();
		try {
			while (cursor.hasNext()) {
				System.out.println(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
	}

	public void test(){
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
			String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
			System.out.println(jsonPrettyPrintString);
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
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

	
	public static void main(String[] args) {
		MongoConnectorMain m = new MongoConnectorMain();
		m.getAllDocuments();
		m.readXMLtoJSON("OTK Sample.xml");
		//m.insertDocument();
		m.getAllDocuments();
		//m.test();
	}

}
