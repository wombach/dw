package org.iea.connector.parser;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bson.BSONObject;
import org.bson.Document;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.storage.MongoDBAccess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.xsd.archimate._3.ModelType;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class Archimate3Parser extends GenericParser {

	private final static Logger LOGGER = Logger.getLogger(Archimate3Parser.class.getName());
	public final static String URI = "http://www.opengroup.org/xsd/archimate/3.0/";

	public Archimate3Parser(){
		this.type = "archimate3";
		this.CONTEXT = "org.opengroup.xsd.archimate._3";
		this.MODEL_CLASS = ModelType.class;
	}

	@Override
	public boolean parseFile(String filename) {
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
				Document doc = insertNodeDocument(l.getJSONObject(i),time);
				String uuid = getUUID(doc);
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

				Document doc = insertRelationDocument(rel, sourceUUID, targetUUID, time);
				String uuid = getUUID(doc);
				map.put(identifier, uuid);
			}
			// files
			//			Document doc = 
			insertFileDocument(xmlJSONObj,time);
			//	}
		}
		return ret;
	}

	@Override
	public void deriveFile(String filename, Date date) {
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
			this.writeJSONtoXML(filename, raw );
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
					if(def.getJSONObject(i).get("identifier").toString().startsWith("propid_wipro_digital_workflow_")){
						flag = true;
						break;
					}
				}
			} else LOGGER.severe("Wrong object type!");
		} 
		if(!flag){
			defs.append("propertyDefinition", new JSONObject(" {\"identifier\": "+
					"\"propidWiproDigitalWorkflowStartDate\", "+
					//					"\"propid_wipro_digital_workflow_start_date\", "+
					" \"type\": \"number\" }"));
			//		"\"name\": \"Wipro start date\", \"type\": \"number\" }"));
			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
					"\"propidWiproDigitalWorkflowEndDate\", "+
					//					"\"propid_wipro_digital_workflow_end_date\", "+
					" 'type': number }"));
			//		"\"name\": \"Wipro end date\", \"type\": \"number\" }"));
			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
					"\"propidWiproDigitalWorkflowIdentifier\", "+
					//					"\"propid_wipro_digital_workflow_identifier\", "+
					" 'type': string }"));
			//		"\"name\": \"Wipro identifier\", \"type\": \"string\" }"));
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
			prop = new JSONObject().put("propertyDefinitionRef","propidWiproDigitalWorkflowStartDate").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_wipro_digital_workflow_start_date").
					put("value", new JSONObject().put("xml:lang","en").put("value", start_date));
			parr.put(prop);
			prop = new JSONObject().put("propertyDefinitionRef","propidWiproDigitalWorkflowEndDate").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_wipro_digital_workflow_end_date").
					put("value", new JSONObject().put("xml:lang","en").put("value", end_date));
			parr.put(prop);
			prop = new JSONObject().put("propertyDefinitionRef","propidWiproDigitalWorkflowIdentifier").
					//					prop = new JSONObject().put("propertyDefinitionRef","propid_wipro_digital_workflow_identifier").
					put("value", new JSONObject().put("xml:lang","en").put("value", id));
			parr.put(prop);
		}
	}

	@Override
	public boolean processXmlString(String str) {
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
				Document doc = insertNodeDocument(l.getJSONObject(i), time);
				String uuid = getUUID(doc);
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
				Document doc = insertRelationDocument(rel, sourceUUID, targetUUID, time);
				String uuid = getUUID(doc);
				map.put(identifier, uuid);
			}
			// files
			//			Document doc = 
			insertFileDocument(xmlJSONObj, time);
			//	}
		}
		return ret;
	}

	@Override
	public String deriveXmlString(Date date) {
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
			return this.writeJSONtoXML( raw );
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
	public int getNodeHash(JSONObject jsonObject) {
		BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
		jsonDoc.removeField("identifier");
		return jsonDoc.hashCode();
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
	public String deriveJsonString(Date date) {
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
	public Object parseJsonString(String str) {
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

	@Override
	public boolean processJsonString(String str) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = new JSONObject(str);
		// check that the file is indeed an archimate file
		long time = System.currentTimeMillis();
		String retMsg = "";
		if( xmlJSONObj!=null && xmlJSONObj.has("model")){
			JSONObject obj = xmlJSONObj.getJSONObject("model");
			//			String xmlns = obj.getString("xmlns");
			//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
			ret = true;
			// nodes
			JSONObject els =  obj.getJSONObject("elements");
			JSONArray l = els.getJSONArray("element");
			LOGGER.info("number of lements: "+l.length());
			els.remove("element");
			for(int i=0;i<l.length();i++){
				JSONObject n = l.getJSONObject(i);
				String identifier = n.getString("identifier");
				Document doc = factory.insertNodeDocument(this, n, time);
				String uuid = getUUID(doc);
				map.put(identifier, uuid);
			}
			// relations
			JSONObject rels =  obj.getJSONObject("relationships");
			l = rels.getJSONArray("relationship");
			LOGGER.info("number of relationships: "+l.length());
			rels.remove("relationship"); 
			Vector<JSONObject> v;
			Vector<JSONObject> v2 = new Vector<JSONObject>();
			HashMap<String,String> relIds = new HashMap<String,String>();
			for(int i=0;i<l.length();i++){
				JSONObject rel = l.getJSONObject(i);
				String identifier = rel.getString("identifier");
//				if (identifier.equals("relation-fcdb1ce9-89c7-e611-8309-5ce0c5d8efd6")){
//					LOGGER.info("found it");
//				}
				relIds.put(identifier, UUID.randomUUID().toString());
				v2.add(rel);
			}
			while(!v2.isEmpty()){
				v = v2;
				v2 = new Vector<JSONObject>();
				while(!v.isEmpty()){
					int i = (int) (Math.random() * v.size());
					JSONObject rel = v.remove(i);
					String identifier = rel.getString("identifier");
//					if (identifier.equals("relation-f41438e9-89c7-e611-8309-5ce0c5d8efd6")){
//						LOGGER.info("found it");
//					}
						String source = rel.getString("source");
					String target = rel.getString("target");
					String sourceUUID = map.get(source);
					if(sourceUUID==null){
						if(relIds.containsKey(source)){
							sourceUUID = relIds.get(source);
						}
					}
					String targetUUID = map.get(target);
					if(targetUUID==null){
						if(relIds.containsKey(target)){
							targetUUID = relIds.get(target);
						}
					}
					if (sourceUUID==null || sourceUUID.isEmpty() || 
							targetUUID==null || targetUUID.isEmpty()){
						String type = rel.getString("type");
						if(sourceUUID == null){
							retMsg += "Relation "+identifier+" ("+type+") related source ID can not be found: "+source+"\n";
						} else if(targetUUID==null){
							retMsg += "Relation "+identifier+" ("+type+") related target ID can not be found: "+target+"\n";
						} 
					} else {
						String uuid = relIds.get(identifier);
						Document doc = factory.insertRelationDocument(this, uuid, rel, sourceUUID, targetUUID, time);
//						map.put(identifier, uuid);
					}
				}
			}
			// files
			//			Document doc = 
			insertFileDocument(xmlJSONObj, time);
			//	}
		}
		LOGGER.severe(retMsg);
		return ret;
	}

}
