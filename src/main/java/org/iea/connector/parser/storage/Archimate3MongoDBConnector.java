package org.iea.connector.parser.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.BSONObject;
import org.bson.BsonArray;
import org.bson.Document;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.storage.MongoDBAccess;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

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
	private static final String DOC_BRANCH = "branch";

	private MongoDBAccess mongo ;

	public Archimate3MongoDBConnector(){
		mongo = new MongoDBAccess();
	}

	public Document enrichDocument( JSONObject obj, String branch, long time, String compStr, int hash){
		String uuid = UUID.randomUUID().toString();
		obj.remove("identifier");
		obj.put("identifier", uuid);
		if(obj.has("properties")){
			JSONArray props = obj.getJSONArray("properties");
			Iterator<Object> it = props.iterator();
			while(it.hasNext()){
				JSONObject prop = (JSONObject) it.next();
				if(prop.getString("propertyDefinitionRef").equals("propidIEAStartDate")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", time);
				} else if(prop.getString("propertyDefinitionRef").equals("propidIEAEndDate")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", -1);
				} else if(prop.getString("propertyDefinitionRef").equals("propidIEAIdentifier")){
					JSONObject val = prop.getJSONObject("value");
					val.remove("value");
					val.put("value", uuid);
				} 
			}
		}

		//JSONArray branchArr = new JSONArray();
		//branchArr.put(branch);
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, parser.getType())
				.append(DOC_ID, uuid)
				.append(DOC_START_DATE, time)
				.append(DOC_END_DATE, -1L)
				.append(DOC_COMPARISON_STRING, compStr)
				.append(DOC_HASH, hash)
				//	.append(DOC_BRANCH, branchArr)
				.append(DOC_BRANCH, branch)
				.append(DOC_RAW, (BSONObject)com.mongodb.util.JSON.parse(obj.toString()));
		return doc;
	}

	protected String getNodeComparisonString(JSONObject jsonObject) {
		JSONObject nameObj = jsonObject.getJSONArray(Archimate3Parser.NAME_TAG).getJSONObject(0);
		String name = nameObj.getString(Archimate3Parser.VALUE_TAG);
		String node_type = jsonObject.getString(Archimate3Parser.TYPE_TAG);
		return parser.getType()+"|"+node_type+"|"+name.toString();
	}

	protected int getNodeHash(JSONObject jsonObject) {
		BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
		jsonDoc.removeField(Archimate3Parser.IDENTIFIER_TAG);
		return jsonDoc.hashCode();
	}


	@Override
	public GenericStorageResult insertNodeDocument(String project, String branch, JSONObject jsonObject, long time) {
		String compStr = getNodeComparisonString(jsonObject);

		int hash = parser.getNodeHash(jsonObject);
		Document doc = null;
		GenericStorageResult ret = new GenericStorageResult();
		boolean insert = false;
		//		long time = System.currentTimeMillis();
		FindIterable<Document> docs = mongo.queryDocument(project, branch, MongoDBAccess.COLLECTION_NODES, DOC_COMPARISON_STRING, compStr, new Date(System.currentTimeMillis()));


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
			doc = enrichDocument( jsonObject, branch, time, compStr, hash);
			mongo.insertDocument(project, branch, MongoDBAccess.COLLECTION_NODES, doc);
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
	public GenericStorageResult insertRelationDocument(String project, String branch, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		Document doc = enrichDocument( jsonObject,branch, time, compStr, hash);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		mongo.insertDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	@Override
	public GenericStorageResult insertViewDocument(String project, String branch, JSONObject jsonObject, long time) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		Document doc = enrichDocument( jsonObject,branch, time, compStr, hash);
		mongo.insertDocument(project, branch, MongoDBAccess.COLLECTION_VIEWS, doc);
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

	@Override
	public void dropDB() {
		mongo.dropCollections();
	}

	@Override
	public void dropProject(String project) {
		mongo.dropProject(project);

	}

	@Override
	public void dropBranch(String project, String branch) {
		mongo.dropBranch(project, branch);
	}

	@Override
	public Document retrieveViewDocument(String project, String branch, long time) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_VIEWS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
			MongoCursor<Document> it = docs.iterator();
			while(it.hasNext()){
				Document doc = it.next();
//				LOGGER.severe("next instance: "+doc.toJson().toString());
				Document raw = (Document) doc.get("raw");
				elem.add(raw);
			}
			Document ret = new Document();
			ret.append(Archimate3Parser.VIEW_TAG, elem);
		return ret;
	}

	@Override
	public Document retrieveNodeDocument(String project, String branch, long time) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_NODES, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
			MongoCursor<Document> it = docs.iterator();
			while(it.hasNext()){
				Document doc = it.next();
//				LOGGER.severe("next instance: "+doc.toJson().toString());
				Document raw = (Document) doc.get("raw");
				elem.add(raw);
			}
			Document ret = new Document();
			ret.append(Archimate3Parser.ELEMENT_TAG, elem);
		return ret;
	}

	@Override
	public Document retrieveRelationDocument(String project, String branch, long time) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
			MongoCursor<Document> it = docs.iterator();
			while(it.hasNext()){
				Document doc = it.next();
//				LOGGER.severe("next instance: "+doc.toJson().toString());
				Document raw = (Document) doc.get("raw");
				elem.add(raw);
			}
			Document ret = new Document();
			ret.append(Archimate3Parser.RELATIONSHIP_TAG, elem);
		return ret;
	}

}
