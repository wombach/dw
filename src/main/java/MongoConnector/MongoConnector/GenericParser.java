package MongoConnector.MongoConnector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.client.FindIterable;

import disco.ProcessMapType;

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
	protected String CONTEXT = null;
	protected Class MODELL_CLASS = null;

	public abstract boolean parseFile(String filename);

	public abstract boolean parseString(String str);

	public abstract void deriveFile(String filename, Date date);

	public abstract String deriveString(Date date);

	public Document enrichDocument( JSONObject obj){
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, type)
				.append(DOC_ID, UUID.randomUUID().toString())
				.append(DOC_START_DATE, System.currentTimeMillis())
				.append(DOC_END_DATE, -1L)
				.append(DOC_RAW, (BSONObject)com.mongodb.util.JSON.parse(obj.toString()));
		return doc;
	}


	public Document insertNodeDocument(JSONObject jsonObject) {
		Document doc = enrichDocument( jsonObject);
		MongoDBAccess mongo = UIControl.getMongo();
		mongo.insertDocument(MongoDBAccess.COLLECTION_NODES, doc);
		// missing handling of updates
		return doc;
	}

	public Document insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID) {
		Document doc = enrichDocument( jsonObject);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		MongoDBAccess mongo = UIControl.getMongo();
		mongo.insertDocument(MongoDBAccess.COLLECTION_RELATIONS, doc);
		// missing handling of updates
		return doc;
	}

	public Document insertFileDocument(JSONObject obj) {
		Document doc = enrichDocument( obj);
		MongoDBAccess mongo = UIControl.getMongo();
		mongo.insertDocument(MongoDBAccess.COLLECTION_FILES, doc);
		// missing handling of updates
		return doc;
	}

	public FindIterable<Document> queryDocument(String col, Date date){
		MongoDBAccess mongo = UIControl.getMongo();
		return mongo.queryDocument(col, DOC_TYPE, type, date);
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
	protected String writeJSONtoXML(JSONObject jobj){
		JAXBContext jaxbContext;
		JAXBElement result = null;
		String ret = "";
		try {
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse JSON
			String st = jobj.toString();
			ByteArrayInputStream in = new ByteArrayInputStream(st.getBytes());

			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			//			unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			StreamSource source2 = new StreamSource(in);
			result = unmarshaller2.unmarshal(source2, MODELL_CLASS );

			// write XML
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			//			marshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			StringWriter out;
			out = new StringWriter();
			// Create the Document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.newDocument();

			//			marshaller.marshal(result, out);
			//			out.close();
			marshaller.marshal(result, document);

			// remove elements without namespace
			Element root = document.getDocumentElement();
			HashMap<Node, Node> m = new HashMap<Node,Node>();
			m.put(root, null);
			while(!m.isEmpty()){
				HashMap<Node, Node> m2 = new HashMap<Node,Node>();
				for( Node n : m.keySet()){
					if(n instanceof Element){
						Node nn = m.get(n);
						if(nn!=null && (n.getNamespaceURI() == null || n.getNamespaceURI().isEmpty())){
							if(nn !=null) nn.removeChild(n);
						} else {
							NodeList nodes = ((Element)n).getChildNodes();
							for(int i=nodes.getLength()-1;i>=0;i--){
								m2.put(nodes.item(i),n);
							}
						}
					}
				}
				m = m2;
			}
			// Output the Document
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result2 = new StreamResult(out);
			t.transform(source, result2);	
			ret = out.toString();
		} catch (JAXBException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	protected void writeJSONtoXML(String filename, JSONObject jobj){
		JAXBContext jaxbContext;
		JAXBElement result = null;
		try {
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse JSON
			String st = jobj.toString();
			ByteArrayInputStream in = new ByteArrayInputStream(st.getBytes());

			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			//			unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");

			StreamSource source2 = new StreamSource(in);
			result = unmarshaller2.unmarshal(source2, MODELL_CLASS );

			// Create the Document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.newDocument();

			// write XML
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			//			marshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			PrintWriter out;
			out = new PrintWriter(filename);
			//			marshaller.marshal(result, out);
			//			out.close();
			marshaller.marshal(result, document);

			// remove elements without namespace
			Element root = document.getDocumentElement();
			HashMap<Node, Node> m = new HashMap<Node,Node>();
			m.put(root, null);
			while(!m.isEmpty()){
				HashMap<Node, Node> m2 = new HashMap<Node,Node>();
				for( Node n : m.keySet()){
					if(n instanceof Element){
						Node nn = m.get(n);
						if(nn!=null && (n.getNamespaceURI() == null || n.getNamespaceURI().isEmpty())){
							if(nn !=null) nn.removeChild(n);
						} else {
							NodeList nodes = ((Element)n).getChildNodes();
							for(int i=nodes.getLength()-1;i>=0;i--){
								m2.put(nodes.item(i),n);
							}
						}
					}
				}
				m = m2;
			}
			// Output the Document
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result2 = new StreamResult(out);
			t.transform(source, result2);
		} catch (JAXBException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
			result = unmarshaller.unmarshal(source, MODELL_CLASS);
		} catch (JAXBException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JSONObject readXMLtoJSON(String filename){
		JSONObject ret = null;
		JAXBContext jaxbContext;
		try{
			File file = new File(filename);
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse XML
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//			unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");

			StreamSource source = new StreamSource(file);
			JAXBElement result = unmarshaller.unmarshal(source, MODELL_CLASS);

			// create JSON
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE,"application/json");
			// Set it to true if you need to include the JSON root element in the JSON output
			marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
			// Set it to true if you need the JSON output to formatted
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//			marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			// Marshal the employee object to JSON and print the output to console
			ByteArrayOutputStream st = new ByteArrayOutputStream();
			marshaller.marshal(result, st);
			ret = new JSONObject(st.toString());

		} catch (JAXBException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			LOGGER.info(filename+" is not an "+CONTEXT+" file because of "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			LOGGER.info(filename+" is not an "+CONTEXT+" file because of "+e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected JSONObject convertXMLtoJSON(String xml){
		JSONObject ret = null;
		JAXBContext jaxbContext;
		try{
			jaxbContext = JAXBContext.newInstance(CONTEXT );
			// parse XML
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//			unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
			StringReader reader = new StringReader(xml);
			StreamSource source = new StreamSource(reader);
			JAXBElement result = unmarshaller.unmarshal(source, MODELL_CLASS);

			// create JSON
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE,"application/json");
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

}