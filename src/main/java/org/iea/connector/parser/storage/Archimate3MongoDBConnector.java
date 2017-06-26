package org.iea.connector.parser.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class Archimate3MongoDBConnector extends GenericParserStorageConnector 
implements GenericParserStorageConnectorManager {

	public final static int PRETTY_PRINT_INDENT_FACTOR = 4;
	public final static String DOC_NAME = "name";
	public final static String DOC_NAME_NODE = "node";
	public final static String DOC_TYPE = "type";
	public final static String DOC_START_DATE = "start_date";
	public final static String DOC_END_DATE = "end_date";
	public final static String DOC_RAW = "raw";
	//public final static String DOC_RAW_ELEMENT = "element";
	public final static String DOC_ID = "id";
	public static final String DOC_HASH = "hash";
	public static final String DOC_BRANCH = "branch";
	public static final String DOC_ORGANIZATION_LABEL ="label";
	public static final String DOC_ORGANIZATION_POSITION = "position";
	public static final String DOC_ORGANIZATION = "organization";
	public static final String DOC_ORGANIZATION_DEFAULT_FOLDER = "default";
	public static final String DOC_ORGANIZATION_CONTENT = "content";

	public static final String PROPID_IEA_HASH = "propidIEAHash";
	public static final String PROPID_IEA_END_DATE = "propidIEAEndDate";
	public static final String PROPID_IEA_START_DATE = "propidIEAStartDate";
	public static final String PROPID_IEA_IDENTIFIER = "propidIEAIdentifier";

	private final static Logger LOGGER = Logger.getLogger(Archimate3MongoDBConnector.class.getName());

	private MongoDBAccess mongo ;

	public Archimate3MongoDBConnector(){
		mongo = new MongoDBAccess();
	}

	public Document enrichDocument( Document jsonObject, String branch, long time, int hash, ArrayList<Document> orgJson){
		//		String uuid = "id-"+UUID.randomUUID().toString();
		//		jsonObject.remove(Archimate3Parser.IDENTIFIER_TAG);
		String uuid = jsonObject.getString(Archimate3Parser.IDENTIFIER_TAG);
		// copying the document has been done to ensure the order of the properties in the document. This is unfortunately
		// necessary since the current moxy implementation has a bug that it is not able to read @xsi_type at an arbitrary 
		// position in the json string. It has to be at the beginning of the string.
		// When the bug is fixed, we can remove this copy operation.
		Document raw = new Document();
		raw.append(Archimate3Parser.IDENTIFIER_TAG, uuid);
		if(jsonObject.containsKey(Archimate3Parser.TYPE_TAG)){
			raw.append(Archimate3Parser.TYPE_TAG, jsonObject.get(Archimate3Parser.TYPE_TAG));
		}
		jsonObject.remove(Archimate3Parser.TYPE_TAG);
		raw.putAll(jsonObject);
		//		jsonObject.put(Archimate3Parser.IDENTIFIER_TAG, uuid);
		if(jsonObject.containsKey(Archimate3Parser.PROPERTIES_TAG)){
			Document propies = (Document) jsonObject.get(Archimate3Parser.PROPERTIES_TAG);
			ArrayList<Document> props = (ArrayList<Document>) propies.get(Archimate3Parser.PROPERTY_TAG);
			Iterator<Document> it = props.iterator();
			while(it.hasNext()){
				Document prop = it.next();
				if(prop.getString(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG).equals(PROPID_IEA_START_DATE)){
					Document val = (Document) prop.get(Archimate3Parser.PROPERTY_VALUE_TAG);
					//					val.remove(Archimate3Parser.VALUE_TAG);
					val.put(Archimate3Parser.VALUE_TAG, String.valueOf(time));
				} else if(prop.getString(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG).equals(PROPID_IEA_END_DATE)){
					Document val = (Document) prop.get(Archimate3Parser.PROPERTY_VALUE_TAG);
					//					val.remove(Archimate3Parser.VALUE_TAG);
					val.put(Archimate3Parser.VALUE_TAG, String.valueOf((long)-1));
				} else if(prop.getString(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG).equals(PROPID_IEA_IDENTIFIER)){
					Document val = (Document) prop.get(Archimate3Parser.PROPERTY_VALUE_TAG);
					//					val.remove(Archimate3Parser.VALUE_TAG);
					val.put("value", uuid);
				} else if(prop.getString(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG).equals(PROPID_IEA_HASH)){
					Document val = (Document) prop.get(Archimate3Parser.PROPERTY_VALUE_TAG);
					//					val.remove(Archimate3Parser.VALUE_TAG);
					val.put("value", hash);
				} 
			}
		} else {
			ArrayList<Document> props = new ArrayList<Document>();
			Document prop = new Document();
			prop.put(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG, PROPID_IEA_START_DATE);
			Document val = new Document("@xml_lang", "en");
			String start = String.valueOf(time);
			val.put(Archimate3Parser.VALUE_TAG, start);
			prop.put(Archimate3Parser.PROPERTY_VALUE_TAG, val);
			props.add(prop);
			prop = new Document();
			prop.put(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG, PROPID_IEA_END_DATE);
			val = new Document("@xml_lang", "en");
			String end = String.valueOf((long)-1);
			val.put(Archimate3Parser.VALUE_TAG, end);
			prop.put(Archimate3Parser.PROPERTY_VALUE_TAG, val);
			props.add(prop);
			prop = new Document();
			prop.put(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG, PROPID_IEA_IDENTIFIER);
			val = new Document("@xml_lang", "en");
			val.put(Archimate3Parser.VALUE_TAG, uuid);
			prop.put(Archimate3Parser.PROPERTY_VALUE_TAG, val);
			props.add(prop);
			prop = new Document();
			prop.put(Archimate3Parser.PROPERTY_DEFINITIONREF_TAG, PROPID_IEA_HASH);
			val = new Document("@xml_lang", "en");
			val.put(Archimate3Parser.VALUE_TAG, hash);
			prop.put(Archimate3Parser.PROPERTY_VALUE_TAG, val);
			props.add(prop);
			raw.put(Archimate3Parser.PROPERTIES_TAG, new Document(Archimate3Parser.PROPERTY_TAG,props));
		}

		//JSONArray branchArr = new JSONArray();
		//branchArr.put(branch);
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, parser.getType())
				.append(DOC_ID, uuid)
				.append(DOC_START_DATE, time)
				.append(DOC_END_DATE, -1L)
				.append(DOC_HASH, hash)
				//	.append(DOC_BRANCH, branchArr)
				.append(DOC_BRANCH, branch)
				.append(DOC_ORGANIZATION,  orgJson)
				//				.append(DOC_RAW, jsonObject);
				.append(DOC_RAW, raw);
		return doc;
	}

	//	protected String getNodeComparisonString(Document jsonObject) {
	//		Document nameObj =   ((ArrayList<Document>) jsonObject.get(Archimate3Parser.NAME_TAG)).get(0);
	//		String name = nameObj.getString(Archimate3Parser.VALUE_TAG);
	//		String node_type = jsonObject.getString(Archimate3Parser.TYPE_TAG);
	//		return parser.getType()+"|"+node_type+"|"+name.toString();
	//	}

	//	protected int getNodeHash(Document jsonObject) {
	//		BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
	//		jsonDoc.removeField(Archimate3Parser.IDENTIFIER_TAG);
	//		return jsonDoc.hashCode();
	//	}

	private ArrayList<Document> createOrganizationPath(Vector<KeyValuePair> org, String branch) {
		ArrayList<Document> root = new ArrayList<Document>();
		if (org!=null && org.size()>0){
			for(KeyValuePair it : org){
				if(it.getKey()!=null && it.getKey().toString().length()>0 && it.getKey()!="null"){
					Document item = new Document();
					item.put(DOC_ORGANIZATION_LABEL, it.getKey());
					item.put(DOC_ORGANIZATION_POSITION, it.getValue());
					root.add(item);
				} else if(it.getValue()!=null && it.getValue().toString().length()>0 && it.getValue() instanceof Integer) {
					Document item = new Document();
					item.put(DOC_ORGANIZATION_LABEL, branch);
					item.put(DOC_ORGANIZATION_POSITION, it.getValue());
					root.add(item);
				}
			}
		}
		if(root.size()==0){
			Document item = new Document();
			item.put(DOC_ORGANIZATION_LABEL, branch);
			item.put(DOC_ORGANIZATION_POSITION, Integer.MAX_VALUE);
			root.add(item);
		}
		return root;
	}

	public Set<String> retrieveAllNodeIDs(String project, String branch){
		//		String query_all_ids = "db."+MongoDBAccess.COLLECTION_NODES+".find({"+
		//							"'"+DOC_BRANCH+"':\""+branch+"\","+
		//							"'"+DOC_END_DATE+"': -1,},{'"+DOC_ID+"':true})."+
		//							"forEach( function(myDoc){print(tojson(myDoc."+DOC_ID+")); })";
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_NODES);
	}

	public Set<String> retrieveAllRelationshipIDs(String project, String branch){
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_RELATIONS);
	}

	public Set<String> retrieveAllViewIDs(String project, String branch){
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_VIEWS);
	}

	@Override
	public GenericStorageResult insertNodeDocument(String project, String branch, Document jsonObject, long time, Vector<KeyValuePair> org) {
		//		String compStr = parser.getNodeComparisonString(jsonObject);

		int hash = parser.getNodeHash(jsonObject);
		Document doc = null;
		GenericStorageResult ret = new GenericStorageResult();
		String uuid = jsonObject.getString(Archimate3Parser.IDENTIFIER_TAG);
		boolean insert = true;
		//		long time = System.currentTimeMillis();
		//		FindIterable<Document> docs = mongo.queryDocument(project, branch, MongoDBAccess.COLLECTION_NODES, DOC_COMPARISON_STRING, compStr, new Date(System.currentTimeMillis()));

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
		ret.setStatusInserted();
		//		}
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		if (insert){
			doc = enrichDocument( jsonObject, branch, time, hash, orgJson);
			mongo.insertDocument(project, MongoDBAccess.COLLECTION_NODES, doc);
			LOGGER.info("the document has been inserted");
		} else {
			LOGGER.info("no update was necessary");
			ret.setStatusUnchanged();
		}
		ret.setDoc(doc);
		return ret;
	}

	@Override
	public GenericStorageResult insertRelationDocument(String project, String branch, Document jsonObject, String sourceUUID, Document sourceJson, String targetUUID, Document targetJson, long time, Vector<KeyValuePair> org) {
		String compStr = parser.getRelationComparisonString(jsonObject);
		int hash = parser.getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, time, hash, orgJson);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		//		.append("source", sourceJson)
		//		.append("target", targetJson);
		
		mongo.insertDocument(project, MongoDBAccess.COLLECTION_RELATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	@Override
	public GenericStorageResult insertOrganizationDocument(String project, String branch, Vector<KeyValuePair> level, ArrayList<Document> labelArr, long time) {
		String compStr = parser.getOrganizationComparisonString(labelArr);
		int hash = parser.getOrganizationHash(labelArr);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(level, branch);
		Document jsonObj = new Document();
		jsonObj.put(DOC_ORGANIZATION_CONTENT, labelArr);
		Document doc = enrichDocument( jsonObj,branch, time, hash, orgJson);
		mongo.insertDocument(project,MongoDBAccess.COLLECTION_ORGANIZATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	}

	@Override
	public GenericStorageResult insertViewDocument(String project, String branch, Document jsonObject, long time, Vector<KeyValuePair> org) {
		String compStr = parser.getViewComparisonString(jsonObject);
		int hash = parser.getViewHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, time, hash, orgJson);
		mongo.insertDocument(project,MongoDBAccess.COLLECTION_VIEWS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		//
		// TODO missing handling of updates
		// 
		return ret;
	} 

	private int getRelationHash(Document jsonObject) {
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
		ret.append(Archimate3Parser.ITEM_TAG, elem);
		return ret;
	}

	private ArrayList<Document> createBSONFromOrganization(Organization org) {
		ArrayList<Document> ret = new ArrayList<Document>();
		Document e = new Document();
		boolean flag = false;
		if(org.getLabel()!=null){
			//			e = 
			e.append(Archimate3Parser.LABEL_TAG, org.getLabel());
			flag=true;
			//			ret.add(e);
		}
		if(org.getLeaves()!=null && !org.getLeaves().isEmpty()){
			//			e = new Document();
			ArrayList<Document> list = new ArrayList<Document>();
			Map<String, String> leaves = org.getLeaves();
			for(String leaf : leaves.values()){
				Document d = new Document();
				list.add(d.append(Archimate3Parser.ORGANIZATIONS_TYPE_IDENTIFIERREF, leaf));
			}
			e.append(Archimate3Parser.ITEM_TAG, list);
			flag= true;
			//			ret.add(e);
		}
		if (flag) ret.add(e);

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

	public void retireNodeDocument(String project, String branch, String uuid, long time){
		retireDocument(project, branch, MongoDBAccess.COLLECTION_NODES, uuid, time);
	}
	
	public void retireRelationshipDocument(String project, String branch, String uuid, long time){
		retireDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, uuid, time);
	}
	
	public void retireViewDocument(String project, String branch, String uuid, long time){
		retireDocument(project, branch, MongoDBAccess.COLLECTION_VIEWS, uuid, time);
	}
	
	protected void retireDocument(String project, String branch, String col, String uuid, long time){
		//	String query_retire = "db."+MongoDBAccess.COLLECTION_NODES+".update({'"+DOC_ID+"': {$eq: \""+uuid+"\"},"+ 
		//	"'"+DOC_BRANCH+"':\""+branch+"\","+
		//	"'"+DOC_END_DATE+"': -1,"+
		//	"'"+DOC_RAW+"."+Archimate3Parser.PROPERTIES_TAG+"."+Archimate3Parser.PROPERTY_TAG+"."+Archimate3Parser.PROPERTY_VALUE_TAG+"."+Archimate3Parser.VALUE_TAG+"' :{$eq: \"-1\"}},"+
		//	"{$set:{'"+DOC_END_DATE+"':NumberLong("+String.valueOf(time)+"), "+
		//	"'"+DOC_RAW+"."+Archimate3Parser.PROPERTIES_TAG+"."+Archimate3Parser.PROPERTY_TAG+".$."+Archimate3Parser.PROPERTY_VALUE_TAG+"."+Archimate3Parser.VALUE_TAG+"': \""+String.valueOf(time)+"\" }} )";
		BasicDBObject query = new BasicDBObject(DOC_ID, uuid).
				append(DOC_BRANCH, branch).
				append(DOC_END_DATE,-1).
				append(DOC_RAW+"."+Archimate3Parser.PROPERTIES_TAG+"."+Archimate3Parser.PROPERTY_TAG+"."+Archimate3Parser.PROPERTY_VALUE_TAG+"."+Archimate3Parser.VALUE_TAG,String.valueOf(-1) );
		BasicDBObject set = new BasicDBObject("$set", new BasicDBObject(DOC_END_DATE, time).
				append(DOC_RAW+"."+Archimate3Parser.PROPERTIES_TAG+"."+Archimate3Parser.PROPERTY_TAG+".$."+Archimate3Parser.PROPERTY_VALUE_TAG+"."+Archimate3Parser.VALUE_TAG, String.valueOf(time)));
		mongo.retireDocument(project, col, query, set);
	}

}
