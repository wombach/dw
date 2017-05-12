package org.iea.connector.parser.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.BSONObject;
import org.bson.Document;
import org.iea.connector.storage.MongoDBAccess;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

import UIControl;

public class Archimate3MongoDBConnector extends GenericParserStorageConnector 
implements GenericParserStorageConnectorManager {
	private final static Logger LOGGER = Logger.getLogger(Archimate3MongoDBConnector.class.getName());

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
	private MongoDBAccess mongo ;
	
	public Archimate3MongoDBConnector(){
		MongoDBAccess mongo = new MongoDBAccess();
	}
	public Document enrichDocument( JSONObject obj, long time, String compStr, int hash){
		String uuid = UUID.randomUUID().toString();
		obj.remove("identifier");
		obj.put("identifier", uuid);
		if(obj.has("properties")){
			JSONArray props = obj.getJSONArray("properties");
			Iterator<Object> it = props.iterator();
			while(it.hasNext()){
				JSONObject prop = (JSONObject) it.next();
				if(prop.getString("propertyDefinitionRef").equals("propidWiproDigitalWorkflowStartDate")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", time);
				} else if(prop.getString("propertyDefinitionRef").equals("propidWiproDigitalWorkflowEndDate")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", -1);
				} else if(prop.getString("propertyDefinitionRef").equals("propidWiproDigitalWorkflowIdentifier")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", uuid);
				} 
			}
		}

		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, parser.getType())
				.append(DOC_ID, uuid)
				.append(DOC_START_DATE, time)
				.append(DOC_END_DATE, -1L)
				.append(DOC_COMPARISON_STRING, compStr)
				.append(DOC_HASH, hash)
				.append(DOC_RAW, (BSONObject)com.mongodb.util.JSON.parse(obj.toString()));
		return doc;
	}

	protected String getNodeComparisonString(JSONObject jsonObject) {
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String node_type = jsonObject.getString("type");
		return parser.getType()+"|"+node_type+"|"+name.toString();
	}

	protected int getNodeHash(JSONObject jsonObject) {
		BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
		jsonDoc.removeField("identifier");
		return jsonDoc.hashCode();
	}


	@Override
	public GenericStorageResult insertNodeDocument(JSONObject jsonObject, long time) {
		String compStr = getNodeComparisonString(jsonObject);
		
		int hash = parser.getNodeHash(jsonObject);
		Document doc = null;
		GenericStorageResult ret = new GenericStorageResult();
		boolean insert = false;
		//		long time = System.currentTimeMillis();
		FindIterable<Document> docs = mongo.queryDocument(MongoDBAccess.COLLECTION_NODES, DOC_COMPARISON_STRING, compStr, new Date(System.currentTimeMillis()));


		//      TODO: fix updates!
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
		//				ret.setStatusUpdated();
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
		LOGGER.warning("the document to be inserted is not known to the collection");
		insert = true;
		ret.setStatusInserted();
		//		}
		if (insert){
			doc = enrichDocument( jsonObject, time, compStr, hash);
			mongo.insertDocument(MongoDBAccess.COLLECTION_NODES, doc);
			LOGGER.info("the document has been inserted");
		} else {
			LOGGER.info("no update was necessary");
			ret.setStatusUnchanged();
		}
		ret.setDoc(doc);
		return ret;
	}

	protected String getRelationComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericStorageResult insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		Document doc = enrichDocument( jsonObject,time, compStr, hash);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		MongoDBAccess mongo = UIControl.getMongo();
		mongo.insertDocument(MongoDBAccess.COLLECTION_RELATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	private int getRelationHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void updateNodeDocument(JSONObject jsonObject, long time) {
		// TODO Auto-generated method stub
	} 
}
