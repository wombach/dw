package org.iea.connector.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.storage.MongoDBAccess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.xsd.archimate.ModelType;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class ArchimateParser extends GenericParser {

	private final static Logger LOGGER = Logger.getLogger(ArchimateParser.class.getName());
	public final static String URI = "http://www.opengroup.org/xsd/archimate";

	public ArchimateParser(){
		this.type = "archimate";
		this.CONTEXT = "org.opengroup.xsd.archimate";
		this.MODEL_CLASS = ModelType.class;
	}

	@Override
	public boolean parseFile(String project, String branch, String filename) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = readXMLtoJSON(filename);
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
					Document doc = insertNodeDocument(project, branch, l.getJSONObject(i),0);
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
					Document doc = insertRelationDocument(project, branch, rel, sourceUUID, targetUUID, 0);
					String uuid = getUUID(doc);
					map.put(identifier, uuid);
				}
				// files
				//			Document doc = 
				insertFileDocument(project, branch, xmlJSONObj,0);
		//	}
		}
		return ret;
	}

	@Override
	public void deriveFile(String project, String branch, String filename, Date date) {
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
			this.writeJSONtoXML(filename, raw );
		}
	} 

	private void addPropertyStandardTypeDefs(JSONObject jobj) {
		/**
		 * "propertydefs": {"propertydef": [
        {
            "identifier": "propid-junctionType",
            "name": "JunctionType",
            "type": "string"
        },
		 */
		JSONObject defs = null;
		if(jobj.has("propertydefs")){
			defs = jobj.getJSONObject("propertydefs");
		} else {
			defs = new JSONObject();
		}
		JSONArray def = null;
		boolean flag = false;
		if(defs.has("propertydef")){
			Object obj = defs.get("propertydef");
			if(obj instanceof JSONObject){
				// if it is a single object it can not our definitions
				def = new JSONArray();
				def.put((JSONObject) obj);
				defs.append("propertydef", def);
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
		} else {
			flag = true;
		}
		if(!flag){
			defs.append("propertydef", new JSONObject(" {\"identifier\": "+
					"\"propid_wipro_digital_workflow_start_date\", "+
					"\"name\": \"Wipro start date\", \"type\": \"Long\" }"));
			defs.append("propertydef", new JSONObject(" {\"identifier\": "+
					"\"propid_wipro_digital_workflow_end_date\", "+
					"\"name\": \"Wipro end date\", \"type\": \"Long\" }"));
			defs.append("propertydef", new JSONObject(" {\"identifier\": "+
					"\"propid_wipro_digital_workflow_identifier\", "+
					"\"name\": \"Wipro identifier\", \"type\": \"String\" }"));
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
			prop = new JSONObject().put("identifierref","propid_wipro_digital_workflow_start_date").
					put("value", new JSONObject().put("xml:lang","en").put("content", start_date));
			parr.put(prop);
			prop = new JSONObject().put("identifierref","propid_wipro_digital_workflow_end_date").
					put("value", new JSONObject().put("xml:lang","en").put("content", end_date));
			parr.put(prop);
			prop = new JSONObject().put("identifierref","propid_wipro_digital_workflow_identifier").
					put("value", new JSONObject().put("xml:lang","en").put("content", id));
			parr.put(prop);
		}
	}

	@Override
	public boolean processXmlString(String project, String branch, String str) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = convertXMLtoJSON(str);
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
					Document doc = insertNodeDocument(project, branch, l.getJSONObject(i),0);
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
					Document doc = insertRelationDocument(project, branch, rel, sourceUUID, targetUUID, 0);
					String uuid = getUUID(doc);
					map.put(identifier, uuid);
				}
				// files
				//			Document doc = 
				insertFileDocument(project, branch, xmlJSONObj,0);
		//	}
		}
		return ret;
	}

	@Override
	public String deriveXmlString(String project, String branch, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNodeComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNodeHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getRelationComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getRelationHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getFileComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getFileHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean processJsonString(String project, String branch,String str) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String deriveJsonString(String project, String branch, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseXmlString(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseJsonString(String project, String branch,String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeObject(Object elm) {
		// TODO Auto-generated method stub
		return false;
	}

}
