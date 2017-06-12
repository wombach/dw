package org.iea.connector.parser;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.bson.BSONObject;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.iea.connector.storage.MongoDBAccess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.xsd.archimate._3.ModelType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import org.iea.util.KeyValuePair;
import org.iea.util.Organization;

public class Archimate3Parser extends GenericParser {

	private final static Logger LOGGER = Logger.getLogger(Archimate3Parser.class.getName());
	public final static String URI = "http://www.opengroup.org/xsd/archimate/3.0/";
	public final static String IDENTIFIER_TAG = "@identifier";
	public final static String MODEL_TAG = "ar3_model";
	public final static String ELEMENTS_TAG = "ar3_elements";
	public final static String ELEMENT_TAG = "ar3_element";
	public final static String RELATIONSHIPS_TAG = "ar3_relationships";
	public final static String RELATIONSHIP_TAG = "ar3_relationship";
	public final static String VIEWS_TAG = "ar3_views";
	public final static String DIAGRAMS_TAG = "ar3_diagrams";
	public final static String VIEW_TAG = "ar3_view";
	public final static String SOURCE_TAG = "@source";
	public final static String TARGET_TAG = "@target";
	public final static String TYPE_TAG = "@xsi_type";
	public final static String CONNECTION_TAG = "ar3_connection";
	public final static String RELATIONSHIPREF_TAG = "@relationshipRef";
	public final static String NODES_TAG = "ar3_node";
	public final static String ELEMENTREF_TAG = "@elementRef";
	public final static String NAME_TAG = "ar3_name";
	public final static String VALUE_TAG = "value";
	public final static String ORGANIZATIONS_TAG = "ar3_organizations";
	public final static String ORGANIZATIONS_TYPE_LABEL = "label";
	public final static String ORGANIZATIONS_TYPE_IDENTIFIERREF = "@identifierRef";
	public final static String ITEM_TAG = "ar3_item";
	public final static String LABEL_TAG = "ar3_label";
	public final static String IDENTIFIERREF_TAG = "@identifierRef";
	public static final String ORGANIZATION_JSON_LABEL = "organization_label";
	public static final String ORGANIZATION_JSON_POSITION = "organization_position";
	public static final String PROPERTIES_TAG = "ar3_properties";
	public static final String PROPERTY_DEFINITION_TAG = "ar3_propertyDefinition";

	public Archimate3Parser(){
		this.type = "archimate3";
		this.CONTEXT = "org.opengroup.xsd.archimate._3";
		this.MODEL_CLASS = ModelType.class;
	}

	@Override
	public boolean parseFile(String project, String branch,String filename) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = readXMLtoJSON(filename);
		LOGGER.info(xmlJSONObj.toString(2));
		long time = System.currentTimeMillis();
		// check that the file is indeed an archimate file
		if( xmlJSONObj!=null && xmlJSONObj.has("model")){
			JSONObject obj = xmlJSONObj.getJSONObject("model");
			//			String xmlns = obj.getString("xmlns");
			//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
			ret = true;
			// nodes
			JSONObject els =  obj.getJSONObject("elements");
			JSONArray l = els.getJSONArray("element");
			els.remove("element");
			for(int i=0;i<l.length();i++){
				String identifier = l.getJSONObject(i).getString("identifier");
				Document doc = insertNodeDocument(project,branch,l.getJSONObject(i),time);
//				String uuid = getUUID(doc);
				String uuid = getUUID(null);
				map.put(identifier, uuid);
			}
			// relations
			JSONObject rels =  obj.getJSONObject("relationships");
			l = rels.getJSONArray("relationship");
			rels.remove("relationship");
			for(int i=0;i<l.length();i++){
				JSONObject rel = l.getJSONObject(i);
				String identifier = rel.getString("identifier");
				String source = (String) rel.remove("source");
				String target = (String) rel.remove("target");
				String sourceUUID = map.get(source);
				String targetUUID = map.get(target);
				rel.put("source", sourceUUID);
				rel.put("target", targetUUID);

				Document doc = insertRelationDocument(project, branch, rel, sourceUUID, targetUUID, time);
//				String uuid = getUUID(doc);
				String uuid = getUUID(null);
				map.put(identifier, uuid);
			}
			// files
			//			Document doc = 
			insertFileDocument(project, branch, xmlJSONObj,time);
			//	}
		}
		return ret;
	}

	@Override
	public void deriveFile(String project, String branch,String filename, Date date) {
		JSONObject ret = null;
		FindIterable<Document> it = queryDocument(MongoDBAccess.COLLECTION_FILES, date);
		Document doc = it.first();
		if(doc!=null){
			String s =doc.toJson();
			ret =  new JSONObject(s);
			JSONObject raw = ret.getJSONObject("raw");
			JSONObject mod = raw.getJSONObject("model");
			JSONObject els =  mod.getJSONObject("elements");
			JSONArray elm = new JSONArray();
			els.put("element", elm);
			JSONObject rels =  mod.getJSONObject("relationships");
			JSONArray rel = new JSONArray();
			rels.put("relationship", rel);
			// derive the identifiers for the standard properties
			//LOGGER.info(prettyPrintJSON(raw));
			addPropertyStandardTypeDefs(mod);
			// mapping to check whether there are missing references
			Vector<String> v = new Vector<String>();

			it = queryDocument(MongoDBAccess.COLLECTION_NODES, date);
			MongoCursor<Document> h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String id = doc.getString("id");
				long start_date = doc.getLong("start_date");
				long end_date = doc.getLong("end_date");
				s =doc.toJson();
				JSONObject ret1 =  new JSONObject(s);
				JSONObject raw1 = ret1.getJSONObject("raw");
				elm.put(raw1);
				enrichNodeWithProperties(raw1, id, start_date, end_date);
				v.add(id);
			}

			it = queryDocument(MongoDBAccess.COLLECTION_RELATIONS, date);
			h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String sourceid = doc.getString("sourceUUID");
				String targetid = doc.getString("targetUUID");
				s =doc.toJson();
				JSONObject ret2 =  new JSONObject(s);
				JSONObject raw2 = ret2.getJSONObject("raw");
				rel.put(raw2);
				if(!v.contains(sourceid)) LOGGER.severe("source node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
				if(!v.contains(targetid)) LOGGER.severe("target node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
			}
			//			LOGGER.info(ret.toString());
			LOGGER.info(prettyPrintJSON(raw));
			//			this.writeJSONtoXML(filename, raw );
		}
	} 

	private void addPropertyStandardTypeDefs(JSONObject jobj) {
		/**
		 * "propertyDefinitions": {"propertyDefinitions": [
        {
            "identifier": "propid-junctionType",
            "name": "JunctionType",
            "type": "string"
        },
		 */
		JSONObject defs = null;
		if(jobj.has("propertyDefinitions")){
			defs = jobj.getJSONObject("propertyDefinitions");
		} else {
			defs = new JSONObject();
			jobj.append("propertyDefinitions", defs);
		}
		JSONArray def = null;
		boolean flag = false;
		if(defs.has("propertyDefinition")){
			Object obj = defs.get("propertyDefinition");
			if(obj instanceof JSONObject){
				// if it is a single object it can not our definitions
				def = new JSONArray();
				def.put((JSONObject) obj);
				defs.append("propertyDefinition", def);
				flag = true;
			} else if(obj instanceof JSONArray){
				def = (JSONArray) obj;
				for(int i=0;i<def.length();i++){
					if(def.getJSONObject(i).get("identifier").toString().startsWith("propid_iea_")){
						flag = true;
						break;
					}
				}
			} else LOGGER.severe("Wrong object type!");
		} 
		if(!flag){
			defs.append("propertyDefinition", new JSONObject(" {\"identifier\": "+
					"\"propidIEAStartDate\", "+
					//					"\"propid_iea_start_date\", "+
					" \"type\": \"number\" }"));
			//		"\"name\": \"iea start date\", \"type\": \"number\" }"));
			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
					"\"propidIEAEndDate\", "+
					//					"\"propid_iea_end_date\", "+
					" 'type': number }"));
			//		"\"name\": \"iea end date\", \"type\": \"number\" }"));
			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
					"\"propidIEAIdentifier\", "+
					//					"\"propid_iea_identifier\", "+
					" 'type': string }"));
			//		"\"name\": \"iea identifier\", \"type\": \"string\" }"));
		}
	}

	private void enrichNodeWithProperties(JSONObject obj, String id, long start_date, long end_date) {
		JSONObject props = null;
		JSONObject prop = null;
		JSONArray parr =  null;
		if(!obj.has("properties")){
			props = new JSONObject();
			obj.put("properties", props);
			parr = new JSONArray();
			props.put("property", parr);
		} else {
			props = obj.getJSONObject("properties");
			Object oobj = props.get("property");
			if(oobj instanceof JSONObject) prop = (JSONObject) oobj;
			else if(oobj instanceof JSONArray) parr = (JSONArray) oobj;
			else LOGGER.severe("not the right object type found");
		}
		if(prop!=null){
			// only a single property in the JSON
			// remove the property and make it a JSONArray
			props.remove("property");
			parr = new JSONArray();
			parr.put(prop);
			props.put("property", parr);
		}
		if(parr!=null){
			prop = new JSONObject().put("propertyDefinitionRef","propidIEAStartDate").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_iea_start_date").
					put("value", new JSONObject().put("xml:lang","en").put("value", start_date));
			parr.put(prop);
			prop = new JSONObject().put("propertyDefinitionRef","propidIEAEndDate").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_iea_end_date").
					put("value", new JSONObject().put("xml:lang","en").put("value", end_date));
			parr.put(prop);
			prop = new JSONObject().put("propertyDefinitionRef","propidIEAIdentifier").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_iea_identifier").
					put("value", new JSONObject().put("xml:lang","en").put("value", id));
			parr.put(prop);
		}
	}

	@Override
	public boolean processXmlString(String project, String branch,String str) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = convertXMLtoJSON(str);
		long time = System.currentTimeMillis();
		// check that the file is indeed an archimate file
		if( xmlJSONObj!=null && xmlJSONObj.has("model")){
			JSONObject obj = xmlJSONObj.getJSONObject("model");
			//			String xmlns = obj.getString("xmlns");
			//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
			ret = true;
			// nodes
			JSONObject els =  obj.getJSONObject("elements");
			JSONArray l = els.getJSONArray("element");
			els.remove("element");
			for(int i=0;i<l.length();i++){
				Document doc = insertNodeDocument(project, branch, l.getJSONObject(i), time);
//				String uuid = getUUID(doc);
				String uuid = getUUID(null);
				String identifier = l.getJSONObject(i).getString("identifier");
				map.put(identifier, uuid);
			}
			// relations
			JSONObject rels =  obj.getJSONObject("relationships");
			l = rels.getJSONArray("relationship");
			rels.remove("relationship");
			for(int i=0;i<l.length();i++){
				JSONObject rel = l.getJSONObject(i);
				String identifier = rel.getString("identifier");
				String source = rel.getString("source");
				String target = rel.getString("target");
				String sourceUUID = map.get(source);
				String targetUUID = map.get(target);
				Document doc = insertRelationDocument(project, branch, rel, sourceUUID, targetUUID, time);
//				String uuid = getUUID(doc);
				String uuid = getUUID(null);
				map.put(identifier, uuid);
			}
			// files
			//			Document doc = 
			insertFileDocument(project, branch, xmlJSONObj, time);
			//	}
		}
		return ret;
	}

	@Override
	public String deriveXmlString(String project, String branch,Date date) {
		JSONObject ret = null;
		FindIterable<Document> it = queryDocument(MongoDBAccess.COLLECTION_FILES, date);
		Document doc = it.first();
		if(doc!=null){
			String s =doc.toJson();
			ret =  new JSONObject(s);
			JSONObject raw = ret.getJSONObject("raw");
			JSONObject mod = raw.getJSONObject("model");
			JSONObject els =  mod.getJSONObject("elements");
			JSONArray elm = new JSONArray();
			els.put("element", elm);
			JSONObject rels =  mod.getJSONObject("relationships");
			JSONArray rel = new JSONArray();
			rels.put("relationship", rel);
			// derive the identifiers for the standard properties
			//LOGGER.info(prettyPrintJSON(raw));
			addPropertyStandardTypeDefs(mod);
			// mapping to check whether there are missing references
			Vector<String> v = new Vector<String>();

			it = queryDocument(MongoDBAccess.COLLECTION_NODES, date);
			MongoCursor<Document> h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String id = doc.getString("id");
				long start_date = doc.getLong("start_date");
				long end_date = doc.getLong("end_date");
				s =doc.toJson();
				JSONObject ret1 =  new JSONObject(s);
				JSONObject raw1 = ret1.getJSONObject("raw");
				elm.put(raw1);
				enrichNodeWithProperties(raw1, id, start_date, end_date);
				v.add(id);
			}

			it = queryDocument(MongoDBAccess.COLLECTION_RELATIONS, date);
			h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String sourceid = doc.getString("sourceUUID");
				String targetid = doc.getString("targetUUID");
				s =doc.toJson();
				JSONObject ret2 =  new JSONObject(s);
				JSONObject raw2 = ret2.getJSONObject("raw");
				rel.put(raw2);
				if(!v.contains(sourceid)) LOGGER.severe("source node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
				if(!v.contains(targetid)) LOGGER.severe("target node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
			}
			//			LOGGER.info(ret.toString());
			//			LOGGER.info(prettyPrintJSON(ret));
			return this.writeJSONtoXML( raw.toString() );
		}
		return "";
	}

	@Override
	public String getNodeComparisonString(JSONObject jsonObject) {
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String node_type = jsonObject.getString("type");
		return type+"|"+node_type+"|"+name.toString();
	}

	@Override
	public int getNodeHash(Document jsonObject) {
		//BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
		jsonObject.remove(IDENTIFIER_TAG);
		return jsonObject.hashCode();
	}
	@Override
	protected String getRelationComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getFileComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getRelationHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getFileHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}


	//	@Override
	//	public boolean processJsonString(String str) {
	//		boolean ret = false;
	//		HashMap<String,String> map = new HashMap<String,String>();
	//		JSONObject xmlJSONObj = new JSONObject(str);
	//		// check that the file is indeed an archimate file
	//		if( xmlJSONObj!=null && xmlJSONObj.has("model")){
	//			JSONObject obj = xmlJSONObj.getJSONObject("model");
	//			//			String xmlns = obj.getString("xmlns");
	//			//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
	//			ret = true;
	//			// nodes
	//			JSONObject els =  obj.getJSONObject("elements");
	//			JSONArray l = els.getJSONArray("element");
	//			els.remove("element");
	//			for(int i=0;i<l.length();i++){
	//				Document doc = insertNodeDocument(l.getJSONObject(i));
	//				String uuid = getUUID(doc);
	//				String identifier = l.getJSONObject(i).getString("identifier");
	//				map.put(identifier, uuid);
	//			}
	//			// relations
	//			JSONObject rels =  obj.getJSONObject("relationships");
	//			l = rels.getJSONArray("relationship");
	//			rels.remove("relationship");
	//			for(int i=0;i<l.length();i++){
	//				JSONObject rel = l.getJSONObject(i);
	//				String identifier = rel.getString("identifier");
	//				String source = rel.getString("source");
	//				String target = rel.getString("target");
	//				String sourceUUID = map.get(source);
	//				String targetUUID = map.get(target);
	//				Document doc = insertRelationDocument(rel, sourceUUID, targetUUID);
	//				String uuid = getUUID(doc);
	//				map.put(identifier, uuid);
	//			}
	//			// files
	//			//			Document doc = 
	//			insertFileDocument(xmlJSONObj);
	//			//	}
	//		}
	//		return ret;
	//	}

	@Override
	public String deriveJsonString(String project, String branch,Date date) {
		JSONObject ret = null;
		FindIterable<Document> it = queryDocument(MongoDBAccess.COLLECTION_FILES, date);
		Document doc = it.first();
		if(doc!=null){
			LOGGER.info("found document");
			String s =doc.toJson();
			LOGGER.info("doc: "+s);
			ret =  new JSONObject(s);
			JSONObject raw = ret.getJSONObject("raw");
			JSONObject mod = raw.getJSONObject("model");
			JSONObject els =  mod.getJSONObject("elements");
			JSONArray elm = new JSONArray();
			els.put("element", elm);
			JSONObject rels =  mod.getJSONObject("relationships");
			JSONArray rel = new JSONArray();
			rels.put("relationship", rel);
			// derive the identifiers for the standard properties
			//LOGGER.info(prettyPrintJSON(raw));
			addPropertyStandardTypeDefs(mod);
			// mapping to check whether there are missing references
			Vector<String> v = new Vector<String>();

			it = queryDocument(MongoDBAccess.COLLECTION_NODES, date);
			MongoCursor<Document> h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String id = doc.getString("id");
				long start_date = doc.getLong("start_date");
				long end_date = doc.getLong("end_date");
				s =doc.toJson();
				JSONObject ret1 =  new JSONObject(s);
				JSONObject raw1 = ret1.getJSONObject("raw");
				elm.put(raw1);
				enrichNodeWithProperties(raw1, id, start_date, end_date);
				v.add(id);
			}

			it = queryDocument(MongoDBAccess.COLLECTION_RELATIONS, date);
			h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String sourceid = doc.getString("sourceUUID");
				String targetid = doc.getString("targetUUID");
				s =doc.toJson();
				JSONObject ret2 =  new JSONObject(s);
				JSONObject raw2 = ret2.getJSONObject("raw");
				rel.put(raw2);
				if(!v.contains(sourceid)) LOGGER.severe("source node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
				if(!v.contains(targetid)) LOGGER.severe("target node referenced by uuid ("+sourceid+") is not contained in the model! Model inconsistent!");;
			}
			//			LOGGER.info(ret.toString());
			//			LOGGER.info(prettyPrintJSON(ret));
			return raw.toString();
		}
		return "";
	}

	@Override
	public Object parseXmlString(String str) {
		JAXBContext jaxbContext;
		ModelType model2 = null;
		try {
			jaxbContext = JAXBContext.newInstance(ModelType.class);
			ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			//		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			StreamSource source2 = new StreamSource(in);
			JAXBElement<ModelType> result = unmarshaller2.unmarshal(source2, ModelType.class);
			model2 = (ModelType) result.getValue();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model2;
	}

	@Override
	public Object parseJsonString(String project, String branch,String str) {
		JAXBContext jaxbContext;
		ModelType model2 = null;
		try {
			jaxbContext = JAXBContext.newInstance(ModelType.class);
			ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			StreamSource source2 = new StreamSource(in);
			JAXBElement<ModelType> result = unmarshaller2.unmarshal(source2, ModelType.class);
			model2 = (ModelType) result.getValue();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model2;
	}

	@Override
	public boolean storeObject(Object elm) {
		// TODO Auto-generated method stub
		return false;
	}

	public String deriveJsonString(String project, String branch, long time){
		//factory.deriveJsonString(, project, branch, date)
		return null;
	}

	@Override
	public boolean processJsonString(String project, String branch, String str) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
//		JSONObject xmlJSONObj = new JSONObject(str);
		Document doc_all = Document.parse(str);
		// check that the file is indeed an archimate file
		long time = System.currentTimeMillis();
		String retMsg = "";
		HashMap<String,Document> nodeMap = new HashMap<String,Document>();
		if( doc_all!=null && doc_all.containsKey(MODEL_TAG)){
			Document obj = (Document) doc_all.get(MODEL_TAG);
			//			String xmlns = obj.getString("xmlns");
			//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
			ret = true;
			// organizations
			ArrayList<Document> orgs = (ArrayList<Document>) obj.get(ORGANIZATIONS_TAG);
			// call a recursion function to parse the tree and add one document per element into the organizations collection
			HashMap<String, Vector<KeyValuePair>> orgMap = new HashMap<String,Vector<KeyValuePair>>();
			createOrganizationLookup(project, branch, orgs, orgMap, time, new Vector<KeyValuePair>());

			//
			// nodes
			Document els = (Document) obj.get(ELEMENTS_TAG);
			ArrayList<Document> l = (ArrayList<Document>) els.get(ELEMENT_TAG);
			LOGGER.info("number of lements: "+l.size());
			els.remove(ELEMENT_TAG);
			for(int i=0;i<l.size();i++){
				Document n = l.get(i);
				String identifier = n.getString(IDENTIFIER_TAG);
				Document doc = factory.insertNodeDocument(this, project, branch, n, time, orgMap.get(identifier));
				String uuid = getUUID(doc);
				ArrayList<Document> nameArr = (ArrayList<Document>) n.get("ar3_name");
				Document nameObj = nameArr.get(0);
				String name = nameObj.getString("value");
				map.put(identifier, uuid);
				Document s = new Document();
				s.put("name", name);
				s.put("time", time);
				s.put("type", getType());
				s.put("branch",branch);
				nodeMap.put(uuid, s );
			}
			//
			// relations
			Document rels =  (Document) obj.get(RELATIONSHIPS_TAG);
			l = (ArrayList<Document>) rels.get(RELATIONSHIP_TAG);
			LOGGER.info("number of relationships: "+l.size());
			rels.remove(RELATIONSHIP_TAG); 
			Vector<Document> v;
			Vector<Document> v2 = new Vector<Document>();
			HashMap<String,String> relIds = new HashMap<String,String>();
			for(int i=0;i<l.size();i++){
				Document rel = l.get(i);
				String identifier = rel.getString(IDENTIFIER_TAG);
				//				if (identifier.equals("relation-fcdb1ce9-89c7-e611-8309-5ce0c5d8efd6")){
				//					LOGGER.info("found it");
				//				}
				relIds.put(identifier, UUID.randomUUID().toString());
				v2.add(rel);
			}
			while(!v2.isEmpty()){
				v = v2;
				v2 = new Vector<Document>();
				while(!v.isEmpty()){
					int i = (int) (Math.random() * v.size());
					Document rel = v.remove(i);
					String identifier = rel.getString(IDENTIFIER_TAG);
					//					if (identifier.equals("relation-f41438e9-89c7-e611-8309-5ce0c5d8efd6")){
					//						LOGGER.info("found it");
					//					}
					String source = rel.getString(SOURCE_TAG);
					String target = rel.getString(TARGET_TAG);
					String sourceUUID = map.get(source);
					if(sourceUUID==null){
						if(relIds.containsKey(source)){
							sourceUUID = relIds.get(source);
						}
					}
					rel.remove(SOURCE_TAG);
					rel.put(SOURCE_TAG, sourceUUID);
					String targetUUID = map.get(target);
					if(targetUUID==null){
						if(relIds.containsKey(target)){
							targetUUID = relIds.get(target);
						}
					}
					rel.remove(TARGET_TAG);
					rel.put(TARGET_TAG, targetUUID);
					if (sourceUUID==null || sourceUUID.isEmpty() || 
							targetUUID==null || targetUUID.isEmpty()){
						String type = rel.getString(TYPE_TAG);
						if(sourceUUID == null){
							retMsg += "Relation "+identifier+" ("+type+") related source ID can not be found: "+source+"\n";
						} else if(targetUUID==null){
							retMsg += "Relation "+identifier+" ("+type+") related target ID can not be found: "+target+"\n";
						} 
					} else {
						String uuid = relIds.get(identifier);
						Document doc = factory.insertRelationDocument(this, project, branch, uuid, rel, sourceUUID, nodeMap.get(sourceUUID), targetUUID, nodeMap.get(targetUUID), time, orgMap.get(identifier));
						map.put(identifier, uuid);
					}
				}
			}

			// views
			if(obj.containsKey(VIEWS_TAG)){
				Document views =  (Document) obj.get(VIEWS_TAG);
				Document diags = (Document) views.get(DIAGRAMS_TAG);
				//			for( int ii=0;ii<diags.length();ii++){
				ArrayList<Document> lo = (ArrayList<Document>) diags.get(VIEW_TAG);
				LOGGER.info("number of views: "+lo.size());
				for( int ii=0;ii<lo.size();ii++){
					Document view = lo.get(ii);
					String view_id = view.getString(IDENTIFIER_TAG);
					String id;
					String uuid = UUID.randomUUID().toString();
					view.put(IDENTIFIER_TAG, uuid);
					map.put(view_id,  uuid);
					if(view.containsKey(CONNECTION_TAG)){
						ArrayList<Document> cons = (ArrayList<Document>) view.get(CONNECTION_TAG);
						for(int jj=0;jj<cons.size();jj++){
							Document ob = cons.get(jj);
							if(ob.containsKey(RELATIONSHIPREF_TAG)){
								String ref = ob.getString(RELATIONSHIPREF_TAG);
								uuid = map.get(ref);
								ob.put(RELATIONSHIPREF_TAG, uuid);
							}
							String src = ob.getString(SOURCE_TAG);
							uuid = map.get(src);
							ob.put(SOURCE_TAG, uuid);
							String trg = ob.getString(TARGET_TAG);
							uuid = map.get(trg);
							ob.put(TARGET_TAG, uuid);
							id = ob.getString(IDENTIFIER_TAG);
							uuid = UUID.randomUUID().toString();
							ob.put(IDENTIFIER_TAG, uuid);
							map.put(id,  uuid);
							//					LOGGER.info(ob.toString());
						}
					}
					if(view.containsKey(NODES_TAG)){
						ArrayList<Document> nods = (ArrayList<Document>) view.get(NODES_TAG);
						for(int jj=0;jj<nods.size();jj++){
							Document ob = nods.get(jj);
							if( ob.containsKey(ELEMENTREF_TAG)){
								String ref = ob.getString(ELEMENTREF_TAG);
								uuid = map.get(ref);
								ob.put(ELEMENTREF_TAG, uuid);
							}
							id = ob.getString(IDENTIFIER_TAG);
							uuid = UUID.randomUUID().toString();
							ob.put(IDENTIFIER_TAG, uuid);
							map.put(id,  uuid);
						}
					}
					uuid = view.getString(IDENTIFIER_TAG);
					Document doc = factory.insertViewDocument(this, project, branch,  uuid, view, time, orgMap.get(view_id));
					//map.put(id, uuid);
				}
			}
			//			rels.remove("relationship"); 
			//			Vector<JSONObject> v;
			//			Vector<JSONObject> v2 = new Vector<JSONObject>();

			// files
			//			Document doc = 
			//insertFileDocument(project, branch,xmlJSONObj, time);
			//	}
		}
		LOGGER.severe(retMsg);
		return ret;
	}


	private void createOrganizationLookup(String project, String branch, ArrayList<Document> orgs, HashMap<String, Vector<KeyValuePair>> orgMap, long time, Vector<KeyValuePair> level) {
		//JSONArray l = orgs.getJSONArray(ITEM_TAG);
		LOGGER.info("number of items on level ("+level.toString()+"): "+orgs.size());
		String value = null;
		for(int ii=0; ii<orgs.size();ii++){
			 Document item = (Document) orgs.get(ii);
			//			if(ite instanceof JSONArray){
			//				JSONArray item;
			//				item = (JSONArray) ite;
			if(item.containsKey(LABEL_TAG)){
				ArrayList<Document> labelArr = (ArrayList<Document>) item.get(LABEL_TAG);
				Document label = labelArr.get(0);
				value = label.getString(VALUE_TAG);
				Vector<KeyValuePair> level_call = (Vector<KeyValuePair>) level.clone();
				KeyValuePair kv = new KeyValuePair(value,ii);
				level_call.add(kv);
				Document doc = factory.insertOrganizationDocument(this, project, branch, level_call, labelArr,  time);
			}
			if(item.containsKey(ITEM_TAG)){
				ArrayList<Document> item2 =  (ArrayList<Document>) item.get(ITEM_TAG);
				Vector<KeyValuePair> level_call = (Vector<KeyValuePair>) level.clone();
				if(value!=null && !value.isEmpty()){
					KeyValuePair kv = new KeyValuePair(value,ii);
					level_call.add(kv);
				}
				createOrganizationLookup(project, branch, item2, orgMap, time, level_call);
			}
			//			} else if(ite instanceof JSONObject){
			//				JSONObject item = (JSONObject) ite;
			if(item.containsKey(IDENTIFIERREF_TAG)){
				String ref = item.getString(IDENTIFIERREF_TAG);
				//	factory.insertOrganizationDocument(this, project, branch, ORGANIZATIONS_TYPE_LABEL, item, level, map.get(ref), time);
				orgMap.put(ref, level);
				//orgs.remove(ii);
			}
			//			}
		}
		//Document doc = factory.insertOrganizationDocument();
	}

	@Override
	public String retrieveJsonString(String project, String branch, Date date) {
		String ret1 = "{	\"ar3_model\": {"+
				"\"ar3_documentation\": [{\"value\": \"Part of the Enterprise Architecture exported to XML\",\"xml_lang\": \"en\"}],"+
				"\"ar3_elements\": \n";
//		String ret2 = ",\"@identifier\": \"model based on query\",\"ar3_name\": [{\"value\": \"Model with query result\",\"@xml_lang\": \"en\"}],"+
//				"\"ar3_propertyDefinitions\": [{\"ar3_propertyDefinition\": [{\"@identifier\": \"propidIEAStartDate\",\"@propertyType\": \"number\"},"+
//				"{\"@identifier\": \"propidIEAEndDate\",\"@propertyType\": \"number\"	},"+
//				"{\"@identifier\": \"propidIEAIdentifier\",\"@propertyType\": \"string\"}]}],\"ar3_relationships\": [";
		String ret2 = "\n,\"@identifier\": \"model based on query\",\"ar3_name\": [{\"value\": \"Model with query result\",\"@xml_lang\": \"en\"}],"+
						"\"ar3_relationships\": [\n";
		String ret3 = "\n],\"@version\": \"1.0\", \n";
		String ret4 = "\n, \"ar3_views\": {\"ar3_diagrams\": \n";
		String ret5 = "\n}}}";

		long time =  date.getTime();
		Organization org = new Organization();
		Document n = factory.retrieveNodeDocument(this, project, branch,time, org);
		Document r = factory.retrieveRelationDocument(this, project, branch, time, org);
		Document v = factory.retrieveViewDocument(this, project, branch, time, org);
		Document o = factory.retrieveOrganizationDocument(this, project, branch, time, org);

		String ret = ret1;
		JsonWriterSettings writerSet = new JsonWriterSettings(true);
		ret = ret+n.toJson(writerSet)+ret2;
		ret = ret+r.toJson(writerSet)+ret3;
//		ret = ret+o.toJson(writerSet)+ret4;
		String str = o.toJson(writerSet);
		// cutting off the leading and tailing brackets since it is not a JSONObject on its own.
		str = str.substring(str.indexOf("{")+1,str.lastIndexOf("}"));
		ret = ret + str + ret4;
		ret = ret+v.toJson(writerSet)+ret5;
		
//		ob = new JSONObject(v);
//		ret = ret+ob.toString(Archimate3MongoDBConnector.PRETTY_PRINT_INDENT_FACTOR)+ret5;
//		JSONObject ob = new JSONObject(n);
//		ret = ret+ob.toString(Archimate3MongoDBConnector.PRETTY_PRINT_INDENT_FACTOR)+ret2;
//		ob = new JSONObject(r);
//		ret = ret+ob.toString(Archimate3MongoDBConnector.PRETTY_PRINT_INDENT_FACTOR)+ret3;
//		ob = new JSONObject(o);
//		String str = ob.toString(Archimate3MongoDBConnector.PRETTY_PRINT_INDENT_FACTOR);
//		// cutting off the leading and tailing brackets since it is not a JSONObject on its own.
//		str = str.substring(str.indexOf("{")+1,str.lastIndexOf("}"));
//		ret = ret+str+ret4;
//		ob = new JSONObject(v);
//		ret = ret+ob.toString(Archimate3MongoDBConnector.PRETTY_PRINT_INDENT_FACTOR)+ret5;
		//		Iterator<Document> it = o.iterator();
		//		while (it.hasNext()){
		//			Document d = it.next();
		//			if(it.hasNext()){
		//				ret=ret+",";
		//			}
		//		}
		//ret = ret+ret4+v.toJson()+ret5; 
		//ob = new JSONObject(ret);
		//ret = ob.toString(4);
		return ret;
	}

	@Override
	public String writeJSONtoXML(String st){
		JAXBContext jaxbContext;
		JAXBElement result = null;
		String ret = "";
		try {
			//			InputStream iStream = GenericParser.class.getClassLoader().getResourceAsStream("META-INF/binding.xml");
			//			Map<String, Object> properties = new HashMap<String, Object>();
			//			properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);

			//			jaxbContext = JAXBContext.newInstance(new Class[] {MODEL_CLASS},properties );
			jaxbContext =  JAXBContext.newInstance(MODEL_CLASS);
			// parse JSON
			//String st = jobj.toString();
			ByteArrayInputStream in = new ByteArrayInputStream(st.getBytes());
			Map<String, String> namespaces = new HashMap<String, String>();

			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
			namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
//			JAXBElement<ModelType> result = unmarshaller2.unmarshal(source, ModelType.class);

			StreamSource source2 = new StreamSource(in);
			result = unmarshaller2.unmarshal(source2, MODEL_CLASS );

			// write XML
			jaxbContext =  JAXBContext.newInstance(MODEL_CLASS);
			Marshaller marshaller = jaxbContext.createMarshaller();
			//			marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
			//			marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
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



}
