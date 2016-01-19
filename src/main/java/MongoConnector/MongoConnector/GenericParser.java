package MongoConnector.MongoConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.BSONObject;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public abstract class GenericParser {
	
	private final static Logger LOGGER = Logger.getLogger(GenericParser.class.getName()); 
	public final static int PRETTY_PRINT_INDENT_FACTOR = 4;
	private final static String DOC_NAME = "name";
	private final static String DOC_NAME_NODE = "node";
	private final static String DOC_TYPE = "type";
	private final static String DOC_START_DATE = "start_date";
	private final static String DOC_END_DATE = "end_date";
	private final static String DOC_RAW = "raw";
	private final static String DOC_RAW_ELEMENT = "element";
	private final static String DOC_ID = "id";
	
	protected String type = null;
	
	public abstract boolean parseFile(String filename);

	public abstract void deriveFile(String filename, Date date);
	
	public Document enrichDocument( JSONObject obj){
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, type)
				.append(DOC_ID, UUID.randomUUID().toString())
				.append(DOC_START_DATE, System.currentTimeMillis())
				.append(DOC_END_DATE, -1)
				.append(DOC_RAW, (BSONObject)com.mongodb.util.JSON.parse(obj.toString()));
		return doc;
	}
	
	
	public Document insertNodeDocument(JSONObject jsonObject) {
		Document doc = enrichDocument( jsonObject);
		UIControl.mongo.insertDocument(MongoDBAccess.COLLECTION_NODES, doc);
		// missing handling of updates
		return doc;
	}
	
	public Document insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID) {
		Document doc = enrichDocument( jsonObject);
		doc.append("sourceUUID", sourceUUID)
			.append("targetUUID", targetUUID);
		UIControl.mongo.insertDocument(MongoDBAccess.COLLECTION_RELATIONS, doc);
		// missing handling of updates
		return doc;
	}
	
	public Document insertFileDocument(JSONObject obj) {
		Document doc = enrichDocument( obj);
		UIControl.mongo.insertDocument(MongoDBAccess.COLLECTION_FILES, doc);
		// missing handling of updates
		return doc;
	}
	
	public FindIterable<Document> queryDocument(String col, Date date){
		return UIControl.mongo.queryDocument(col, DOC_TYPE, type, date);
	}

	
	public String getUUID(Document doc){
		return doc.getString(DOC_ID);
	}

	public String prettyPrintJSON(JSONObject xmlJSONObj){
		String ret = "";
		try {
			ret = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
		} catch (JSONException je) {
			LOGGER.severe(je.toString());
		}
		return ret;
	}
	
	protected void writeJSONtoXML(String filename, Document doc){
		File fXmlFile = new File(filename);
		String xml = XML.toString(doc.toJson());
	    System.out.println(xml); 
	}

	protected JSONObject readXMLtoJSON(String filename){
		JSONObject ret = null;
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
			ret = XML.toJSONObject(stringBuilder.toString());

		} catch (JSONException je) {
			System.out.println(je.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}