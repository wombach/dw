package org.iea.connector.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.bson.BSONObject;
import org.bson.Document;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.storage.MongoDBAccess;
import org.iea.connector.storage.MongoDBSingleton;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public abstract class GenericParser {

	private final static Logger LOGGER = Logger.getLogger(GenericParser.class.getName()); 
	public final static int PRETTY_PRINT_INDENT_FACTOR = 4;
	public final static String DOC_NAME = "name";
	public final static String DOC_NAME_NODE = "node";
	public final static String DOC_TYPE = "type";
	public final static String DOC_START_DATE = "start_date";
	public final static String DOC_END_DATE = "end_date";
	public final static String DOC_RAW = "raw";
	public final static String DOC_RAW_ELEMENT = "element";
	public final static String DOC_ID = "id";
	public final static String DOC_COMPARISON_STRING = "comparison_string";
	public static final String DOC_HASH = "hash";

	protected String type = null;
	protected String CONTEXT = null;
	protected Class MODEL_CLASS = null;
	protected Map<String, String> namespaces;
	protected ParserFactory factory;

	public GenericParser(){
		namespaces = new HashMap<String, String>();
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "ns1");
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ns0");
	}
	
	public abstract boolean parseFile(String filename, String branch, String filename2);

	public abstract Object parseXmlString(String str);
	public abstract Object parseJsonString(String str, String branch, String str2);
	public abstract boolean storeObject(Object elm);
	
	public boolean processXmlString(String str, String branch, String str2){
		boolean ret = false;
		Object elm = parseXmlString(str);
		if(elm!=null){
			ret = storeObject(elm);
		}
		return ret;	
	};
	
	public abstract  boolean processJsonString(String project, String branch, String json);
//		boolean ret = false;
////		Object elm = parseJsonString(str);
////		if(elm!=null){
//			ret = processJson();
////		}		
//		return ret;
//	}

	public abstract void deriveFile(String project, String branch, String filename, Date date);

	public abstract String deriveXmlString(String project, String branch, Date date);
	public abstract String deriveJsonString(String project, String branch, Date date);

	public Document enrichDocument( JSONObject obj, long time, String compStr, int hash){
		String uuid = "id-"+UUID.randomUUID().toString();
		obj.remove("identifier");
		obj.put("identifier", uuid);
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, type)
				.append(DOC_ID, uuid)
				.append(DOC_START_DATE, time)
				.append(DOC_END_DATE, -1L)
				.append(DOC_COMPARISON_STRING, compStr)
				.append(DOC_HASH, hash)
				.append(DOC_RAW, (BSONObject)com.mongodb.util.JSON.parse(obj.toString()));
		return doc;
	}

	abstract public String getNodeComparisonString(Document jsonObject);
	abstract public int getNodeHash(Document jsonObject);

	public Document insertNodeDocument(String project, String branch,JSONObject jsonObject, long time) {
//		String compStr = getNodeComparisonString(jsonObject);
//		//MongoDBAccess mongo = new MongoDBAccess();
//		int hash = getNodeHash(jsonObject);
//		Document doc = null;
//		boolean insert = false;
////		long time = System.currentTimeMillis();
//		FindIterable<Document> docs = mongo.queryDocument(org.iea.connector.storage.MongoDBAccess.COLLECTION_NODES, DOC_COMPARISON_STRING, compStr, new Date(System.currentTimeMillis()));
//		if(docs !=null && docs.iterator()!=null && docs.iterator().hasNext()){
//			LOGGER.warning("the document to be inserted has at least one element in the collection with the same comparison string");
//			MongoCursor<Document> it = docs.iterator();
//			doc = it.next();
//			if(it.hasNext()) {
//				LOGGER.severe("Comparison strings are supposed to be unique! The database is corrupted!");
//			}
//			String uuid = doc.getString(DOC_ID);
//			int docHash = doc.getInteger(DOC_HASH, 0);
//			if(hash!=docHash) {
//				insert = true;
//				// update existing node by marking it as expired
//
//				// check whether the update is allowed or whether there is a conflict!
//				// a version conflict exists if the end date in the document to be updated is not -1
//				if(doc.getLong(DOC_END_DATE)==-1){
//					mongo.updateDocument(MongoDBAccess.COLLECTION_NODES, DOC_ID, uuid, DOC_END_DATE, time);
//				} else {
//					LOGGER.severe("there is a conflict in the versions); the update has not been completed.");
//				}
//			}
//
//		} else {
//			LOGGER.warning("the document to be inserted is not known to the collection");
//			insert = true;
//		}
//		if (insert){
//			doc = enrichDocument( jsonObject, time, compStr, hash);
//			mongo.insertDocument(MongoDBAccess.COLLECTION_NODES, doc);
//			LOGGER.info("the document has been inserted");
//		} else LOGGER.info("no update was necessary");
//		return doc;
		return null;
	}

	public abstract String getRelationComparisonString(Document jsonObject);
	public abstract int getRelationHash(Document jsonObject);

	public Document insertRelationDocument(String project, String branch,JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
//		String compStr = getRelationComparisonString(jsonObject);
//		int hash = getRelationHash(jsonObject);
//		Document doc = enrichDocument( jsonObject,time, compStr, hash);
//		doc.append("sourceUUID", sourceUUID)
//		.append("targetUUID", targetUUID);
//		mongo.insertDocument(MongoDBAccess.COLLECTION_RELATIONS, doc);
//		// missing handling of updates
//		return doc;
		return null;
	}

	abstract protected String getFileComparisonString(JSONObject jsonObject);
	abstract protected int getFileHash(JSONObject jsonObject);

//	public Document insertOrganizationDocument(String project, String branch,String type, JSONObject jsonObject, Vector<String> level, String refUUID, long time) {
//		return null;
//	}
//
//	abstract protected String getOrganizationComparisonString(JSONObject jsonObject);
//	abstract protected int getOrganizationHash(JSONObject jsonObject);

	public Document insertFileDocument(String project, String branch,JSONObject obj, long time) {
//		String compStr = getFileComparisonString(obj);
//		int hash = getFileHash(obj);
//		Document doc = enrichDocument( obj, time, compStr, hash);
////		MongoDBAccess mongo = UIControl.getMongo();
//		mongo.insertDocument(MongoDBAccess.COLLECTION_FILES, doc);
//		// missing handling of updates
//		return doc;
		return null;
	}

	public FindIterable<Document> queryDocument(String col, Date date){
////		MongoDBAccess mongo = UIControl.getMongo();
//		return mongo.queryDocument(col, DOC_TYPE, type, date);
		return null;
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
	
	public abstract String writeJSONtoXML(String st);
//	protected String writeJSONtoXML(JSONObject jobj){
//		JAXBContext jaxbContext;
//		JAXBElement result = null;
//		String ret = "";
//		try {
////			InputStream iStream = GenericParser.class.getClassLoader().getResourceAsStream("META-INF/binding.xml");
////			Map<String, Object> properties = new HashMap<String, Object>();
////			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);
//
////			jaxbContext = JAXBContext.newInstance(new Class[] {MODEL_CLASS},properties );
//			jaxbContext =  JAXBContext.newInstance(MODEL_CLASS);
//			// parse JSON
//			String st = jobj.toString();
//			ByteArrayInputStream in = new ByteArrayInputStream(st.getBytes());
//
//			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
//			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
//			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
//			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
//			StreamSource source2 = new StreamSource(in);
//			result = unmarshaller2.unmarshal(source2, MODEL_CLASS );
//
//			// write XML
//			jaxbContext =  JAXBContext.newInstance(MODEL_CLASS);
//			Marshaller marshaller = jaxbContext.createMarshaller();
//			marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
//			marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//			//			marshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
//			StringWriter out;
//			out = new StringWriter();
//			// Create the Document
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			org.w3c.dom.Document document = db.newDocument();
//
//			//			marshaller.marshal(result, out);
//			//			out.close();
//			marshaller.marshal(result, document);
//
//			// remove elements without namespace
//			Element root = document.getDocumentElement();
//			HashMap<Node, Node> m = new HashMap<Node,Node>();
//			m.put(root, null);
//			while(!m.isEmpty()){
//				HashMap<Node, Node> m2 = new HashMap<Node,Node>();
//				for( Node n : m.keySet()){
//					if(n instanceof Element){
//						Node nn = m.get(n);
//						if(nn!=null && (n.getNamespaceURI() == null || n.getNamespaceURI().isEmpty())){
//							if(nn !=null) nn.removeChild(n);
//						} else {
//							NodeList nodes = ((Element)n).getChildNodes();
//							for(int i=nodes.getLength()-1;i>=0;i--){
//								m2.put(nodes.item(i),n);
//							}
//						}
//					}
//				}
//				m = m2;
//			}
//			// Output the Document
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer t = tf.newTransformer();
//			DOMSource source = new DOMSource(document);
//			StreamResult result2 = new StreamResult(out);
//			t.transform(source, result2);	
//			ret = out.toString();
//		} catch (JAXBException  e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return ret;
//	}
//
//	protected void writeJSONtoXML(String filename, JSONObject jobj){
//		try {
//			String st =  writeJSONtoXML(jobj);
//			// Output the Document
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer t;
//			t = tf.newTransformer();
//			ByteArrayInputStream in = new ByteArrayInputStream(st.getBytes());
//			StreamSource source = new StreamSource(in);
//			PrintWriter out;
//			out = new PrintWriter(filename);
//			StreamResult result2 = new StreamResult(out);
//			t.transform(source, result2);
//		} catch (TransformerConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JAXBElement readXMLtoJAXB(String filename){
		JAXBContext jaxbContext;
		JAXBElement result = null;
		try{
			File file = new File(filename);
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse XML
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//			unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			StreamSource source = new StreamSource(file);
			result = unmarshaller.unmarshal(source, MODEL_CLASS);
		} catch (JAXBException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JSONObject readXMLtoJSON(String filename){
		String content;
		try {
			content = new String(Files.readAllBytes(Paths.get(filename)));
			return convertXMLtoJSON(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JSONObject convertXMLtoJSON(String xml){
		JSONObject ret = null;
		JAXBContext jaxbContext;
		try{
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse XML
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
//			unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');		
//			unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			StringReader reader = new StringReader(xml);
			StreamSource source = new StreamSource(reader);
			JAXBElement result = unmarshaller.unmarshal(source, MODEL_CLASS);

			// create JSON
//			InputStream iStream = GenericParser.class.getClassLoader().getResourceAsStream("META-INF/binding.xml");
//			Map<String, Object> properties = new HashMap<String, Object>();
//			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);
//			jaxbContext = JAXBContext.newInstance(new Class[] {MODEL_CLASS},properties );

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE,"application/json");
//			marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
//			marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			
			// Set it to true if you need to include the JSON root element in the JSON output
			marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
			// Set it to true if you need the JSON output to formatted
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//			marshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");

			// Marshal the employee object to JSON and print the output to console
			ByteArrayOutputStream st = new ByteArrayOutputStream();
			marshaller.marshal(result, st);
			ret = new JSONObject(st.toString());

		} catch (JAXBException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			LOGGER.info("provided XML is not an "+CONTEXT+" because of "+e.getMessage());
		} catch (Exception e){
			LOGGER.info("provided XML is not an "+CONTEXT+" because of "+e.getMessage());
		}
		return ret;
	}

	public String getType(){
		return type;
	}

	public void setFactory(ParserFactory parserFactory) {
		this.factory = parserFactory; 
	}

	public abstract String retrieveJsonString(String project, String branch, Date date);

	public abstract String getOrganizationComparisonString(ArrayList<Document> labelArr);

	public abstract int getOrganizationHash(ArrayList<Document> labelArr);

	public abstract String getViewComparisonString(Document jsonObject);
	
	public abstract int getViewHash(Document jsonObject);

}