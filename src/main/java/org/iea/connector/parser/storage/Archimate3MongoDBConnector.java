package org.iea.connector.parser.storage;

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

import javax.xml.bind.JAXBElement;

import org.bson.BSONObject;
import org.bson.BsonArray;
import org.bson.Document;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.storage.MongoDBAccess;
import org.iea.util.KeyValuePair;
import org.iea.util.MapUtil;
import org.iea.util.Organization;
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
	//public final static String DOC_RAW_ELEMENT = "element";
	public final static String DOC_ID = "id";
	public final static String DOC_COMPARISON_STRING = "comparison_string";
	public static final String DOC_HASH = "hash";
	public static final String DOC_BRANCH = "branch";
	public static final String DOC_ORGANIZATION_LABEL ="label";
	public static final String DOC_ORGANIZATION_POSITION = "position";
	public static final String DOC_ORGANIZATION = "organization";
	public static final String DOC_ORGANIZATION_DEFAULT_FOLDER = "default";
	private static final String DOC_ORGANIZATION_CONTENT = "content";

	private MongoDBAccess mongo ;

	public Archimate3MongoDBConnector(){
		mongo = new MongoDBAccess();
	}

	public Document enrichDocument( JSONObject obj, String branch, long time, String compStr, int hash, JSONArray orgJson){
		String uuid = UUID.randomUUID().toString();
		//obj.remove(Archimate3Parser.IDENTIFIER_TAG);
		obj.put(Archimate3Parser.IDENTIFIER_TAG, uuid);
		if(obj.has(Archimate3Parser.PROPERTIES_TAG)){
			JSONArray props = obj.getJSONArray(Archimate3Parser.PROPERTIES_TAG);
			Iterator<Object> it = props.iterator();
			while(it.hasNext()){
				JSONObject prop = (JSONObject) it.next();
				if(prop.getString(Archimate3Parser.PROPERTY_DEFINITION_TAG).equals("propidIEAStartDate")){
					JSONObject val = prop.getJSONObject(Archimate3Parser.VALUE_TAG);
					val.remove(Archimate3Parser.VALUE_TAG);
					val.put(Archimate3Parser.VALUE_TAG, time);
				} else if(prop.getString(Archimate3Parser.PROPERTY_DEFINITION_TAG).equals("propidIEAEndDate")){
					JSONObject val = prop.getJSONObject(Archimate3Parser.VALUE_TAG);
					val.remove(Archimate3Parser.VALUE_TAG);
					val.put(Archimate3Parser.VALUE_TAG, -1);
				} else if(prop.getString(Archimate3Parser.PROPERTY_DEFINITION_TAG).equals("propidIEAIdentifier")){
					JSONObject val = prop.getJSONObject(Archimate3Parser.VALUE_TAG);
					val.remove(Archimate3Parser.VALUE_TAG);
					val.put("value", uuid);
				} 
			}
		} else {
			// TODO add a default property
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
				.append(DOC_ORGANIZATION,  (BSONObject)com.mongodb.util.JSON.parse(orgJson.toString()))
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

	private JSONArray createOrganizationPath(Vector<KeyValuePair> org, String branch) {
		JSONArray root = new JSONArray();
		if (org!=null && org.size()>0){
			for(KeyValuePair it : org){
				if(it.getKey()!=null && it.getKey().toString().length()>0 && it.getKey()!="null"){
					JSONObject item = new JSONObject();
					item.put(DOC_ORGANIZATION_LABEL, it.getKey());
					item.put(DOC_ORGANIZATION_POSITION, it.getValue());
					root.put(item);
				} else if(it.getValue()!=null && it.getValue().toString().length()>0 && it.getValue() instanceof Integer) {
					JSONObject item = new JSONObject();
					item.put(DOC_ORGANIZATION_LABEL, branch);
					item.put(DOC_ORGANIZATION_POSITION, it.getValue());
					root.put(item);
				}
			}
		}
		if(root.length()==0){
			JSONObject item = new JSONObject();
			item.put(DOC_ORGANIZATION_LABEL, branch);
			item.put(DOC_ORGANIZATION_POSITION, Integer.MAX_VALUE);
			root.put(item);
		}
		return root;
	}

	@Override
	public GenericStorageResult insertNodeDocument(String project, String branch, JSONObject jsonObject, long time, Vector<KeyValuePair> org) {
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
		JSONArray orgJson = createOrganizationPath(org, branch);
		if (insert){
			doc = enrichDocument( jsonObject, branch, time, compStr, hash, orgJson);
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
	public GenericStorageResult insertRelationDocument(String project, String branch, JSONObject jsonObject, String sourceUUID, JSONObject sourceJson, String targetUUID, JSONObject targetJson, long time, Vector<KeyValuePair> org) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		JSONArray orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, time, compStr, hash, orgJson);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		//		.append("source", sourceJson)
		//		.append("target", targetJson);
		mongo.insertDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	@Override
	public GenericStorageResult insertOrganizationDocument(String project, String branch, Vector<KeyValuePair> level, JSONArray labelArr, long time) {
		String compStr = getOrganizationComparisonString(labelArr);
		int hash = getOrganizationHash(labelArr);
		GenericStorageResult ret = new GenericStorageResult();
		JSONArray orgJson = createOrganizationPath(level, branch);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DOC_ORGANIZATION_CONTENT, labelArr);
		Document doc = enrichDocument( jsonObj,branch, time, compStr, hash, orgJson);
		mongo.insertDocument(project, branch, MongoDBAccess.COLLECTION_ORGANIZATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	private int getOrganizationHash(JSONArray labelArr) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String getOrganizationComparisonString(JSONArray labelArr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericStorageResult insertViewDocument(String project, String branch, JSONObject jsonObject, long time, Vector<KeyValuePair> org) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		JSONArray orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, time, compStr, hash, orgJson);
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
	public Document retrieveViewDocument(String project, String branch, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_VIEWS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());
			Document raw = (Document) doc.get("raw");
			ArrayList<Document> nameArr = (ArrayList<Document>) raw.get(Archimate3Parser.NAME_TAG);
			String name = "";
			if(nameArr!=null && !nameArr.isEmpty()){
				Document nameObj = nameArr.get(0);
				name = nameObj.getString("value");
			}
			ArrayList<Document> oDoc = (ArrayList<Document>) doc.get(DOC_ORGANIZATION);
			Organization item  = org;
			for(int ii=0;ii<oDoc.size();ii++){
				String na = oDoc.get(ii).getString(DOC_ORGANIZATION_LABEL);
				int pos = oDoc.get(ii).getInteger(DOC_ORGANIZATION_POSITION);
				if(!item.contains(na)){
					item.addChild(na, new Organization(na),pos);
				} else {
					item.setChildPosition(na, pos);
				}
				item = item.getChildByName(na);
			}
			item.addLeaf(name, doc.getString(DOC_ID));
			elem.add(raw);
		}
		Document ret = new Document();
		ret.append(Archimate3Parser.VIEW_TAG, elem);
		return ret;
	}

	@Override
	public Document retrieveNodeDocument(String project, String branch, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_NODES, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());
			Document raw = (Document) doc.get(DOC_RAW);
			ArrayList<Document> nameArr = (ArrayList<Document>) raw.get(Archimate3Parser.NAME_TAG);
			String name = "";
			if(nameArr!=null && !nameArr.isEmpty()){
				Document nameObj = nameArr.get(0);
				name = nameObj.getString("value");
			}
			ArrayList<Document> oDoc = (ArrayList<Document>) doc.get(DOC_ORGANIZATION);
			Organization item  = org;
			for(int ii=0;ii<oDoc.size();ii++){
				String na = oDoc.get(ii).getString(DOC_ORGANIZATION_LABEL);
				int pos = oDoc.get(ii).getInteger(DOC_ORGANIZATION_POSITION);
				if(!item.contains(na)){
					item.addChild(na, new Organization(na),pos);
				} else {
					item.setChildPosition(na, pos);
				}
				item = item.getChildByName(na);
			}
			item.addLeaf(name, doc.getString(DOC_ID));
			elem.add(raw);
		}
		Document ret = new Document();
		ret.append(Archimate3Parser.ELEMENT_TAG, elem);
		return ret;
	}

	@Override
	public Document retrieveRelationDocument(String project, String branch, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());
			Document raw = (Document) doc.get("raw");
			ArrayList<Document> oDoc = (ArrayList<Document>) doc.get(DOC_ORGANIZATION);
			Organization item  = org;
			//			for(int ii=0;ii<oDoc.size();ii++){
			//				String na = oDoc.get(ii).getString(DOC_ORGANIZATION_LABEL);
			//				int pos = oDoc.get(ii).getInteger(DOC_ORGANIZATION_POSITION);
			//				item.setChildPosition(na, pos);
			//				item = item.getChildByName(na);
			//			}
			//			item.addLeaf(doc.getString(DOC_ID), doc.getString(DOC_ID));
			//			elem.add(raw);

			for(int ii=0;ii<oDoc.size();ii++){
				String na = oDoc.get(ii).getString(DOC_ORGANIZATION_LABEL);
				int pos = oDoc.get(ii).getInteger(DOC_ORGANIZATION_POSITION);
				if(!item.contains(na)){
					item.addChild(na, new Organization(na),pos);
				} else {
					item.setChildPosition(na, pos);
				}
				item = item.getChildByName(na);
			}
			item.addLeaf(doc.getString(DOC_ID), doc.getString(DOC_ID));
			elem.add(raw);
		}
		Document ret = new Document();
		ret.append(Archimate3Parser.RELATIONSHIP_TAG, elem);
		return ret;
	}

	@Override
	public Document retrieveOrganizationDocument(String project, String branch, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_ORGANIZATIONS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());

			Document raw = (Document) doc.get("raw");
			ArrayList<Document> label = (ArrayList<Document>) raw.get(DOC_ORGANIZATION_CONTENT);
			ArrayList<Document> orgs = (ArrayList<Document>) doc.get(DOC_ORGANIZATION);
			Organization item = org;
			for(Document oDoc : orgs){
				String lab = oDoc.getString(DOC_ORGANIZATION_LABEL);
				int pos = oDoc.getInteger(DOC_ORGANIZATION_POSITION);
				if(!item.contains(lab)){
					item.addChild(lab, new Organization(lab),pos);
				} else {
					item.setChildPosition(lab, pos);
				}
				item = item.getChildByName(lab);
				item.setChildPosition(lab, pos);
			}
			item.setLabel(label);
		}
		Document ret = new Document();
		elem = createBSONFromOrganization(org);
		ret.append(Archimate3Parser.ORGANIZATIONS_TAG, elem);
		return ret;
	}

	private ArrayList<Document> createBSONFromOrganization(Organization org) {
		ArrayList<Document> ret = new ArrayList<Document>();
		Document e;
		if(org.getLabel()!=null){
			e = new Document();
			e.append(Archimate3Parser.ORGANIZATIONS_TYPE_LABEL, org.getLabel());
			ret.add(e);
		}
		if(org.getLeaves()!=null && !org.getLeaves().isEmpty()){
			e = new Document();
			ArrayList<Document> list = new ArrayList<Document>();
			Map<String, String> leaves = org.getLeaves();
			for(String leaf : leaves.values()){
				Document d = new Document();
				list.add(d.append(Archimate3Parser.ORGANIZATIONS_TYPE_IDENTIFIERREF, leaf));
			}
			e.append(Archimate3Parser.ITEM_TAG, list);
			ret.add(e);
		}
		List<Organization> list = org.getChildren();
		if(list != null && !list.isEmpty()){
			for(Organization o : list){
				if(o!=null){
					e = new Document();
					e.append(Archimate3Parser.ITEM_TAG, createBSONFromOrganization(o));
					ret.add(e);
				}
			}
		}
		return ret;
	}


}
