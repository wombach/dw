package MongoConnector.MongoConnector;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class ArchimateParser extends GenericParser {

	private final static Logger LOGGER = Logger.getLogger(ArchimateParser.class.getName());
	public final static String URI = "http://www.opengroup.org/xsd/archimate";

	public ArchimateParser(){
		this.type = "archimate";
	}

	@Override
	public boolean parseFile(String filename) {
		boolean ret = false;
		HashMap<String,String> map = new HashMap<String,String>();
		JSONObject xmlJSONObj = readXMLtoJSON(filename);
		// check that the file is indeed an archimate file
		JSONObject obj = xmlJSONObj.getJSONObject("model");
		String xmlns = obj.getString("xmlns");
		if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
			ret = true;
			// nodes
			JSONObject els =  obj.getJSONObject("elements");
			JSONArray l = els.getJSONArray("element");
			els.remove("element");
			for(int i=0;i<l.length();i++){
				Document doc = insertNodeDocument(l.getJSONObject(i));
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
				Document doc = insertRelationDocument(rel, sourceUUID, targetUUID);
				String uuid = getUUID(doc);
				map.put(identifier, uuid);
			}
			// files
//			Document doc = 
			insertFileDocument(xmlJSONObj);
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
			// mapping to check whether there are missing references
			Vector<String> v = new Vector<String>();
			
			it = queryDocument(MongoDBAccess.COLLECTION_NODES, date);
			MongoCursor<Document> h = it.iterator();
			while(h.hasNext()){
				doc = h.next();
				String id = doc.getString("id");
				s =doc.toJson();
				JSONObject ret1 =  new JSONObject(s);
				JSONObject raw1 = ret1.getJSONObject("raw");
				elm.put(raw1);
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
			LOGGER.info(ret.toString());
		}
	}

}
