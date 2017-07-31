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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
	public static final String DOC_END_USER = "end_user";
	public static final String DOC_START_USER = "start_user";
	public static final String DOC_VERSION = "version";

	public static final String PROPID_IEA_HASH = "propidIEAHash";
	public static final String PROPID_IEA_END_DATE = "propidIEAEndDate";
	public static final String PROPID_IEA_START_DATE = "propidIEAStartDate";
	public static final String PROPID_IEA_IDENTIFIER = "propidIEAIdentifier";
	public static final ImmutableMap<String,String> PROPID_MAP = ImmutableMap.of(PROPID_IEA_HASH,"string",
																				  PROPID_IEA_END_DATE,"number",
																				  PROPID_IEA_START_DATE,"number",
																				  PROPID_IEA_IDENTIFIER, "string" );

	private final static Logger LOGGER = Logger.getLogger(Archimate3MongoDBConnector.class.getName());
	private static final String DOC_ELEMENTS_LIST = "element_list";
	private static final String DOC_RELATIONS_LIST = "relationship_list";
	private static final String DOC_VIEWS_LIST = "view_list";
	
	private MongoDBAccess mongo ;

	public Archimate3MongoDBConnector(){
		mongo = new MongoDBAccess();
	}

	public Document enrichDocument( Document jsonObject, String branch, String user, long time, int hash, ArrayList<Document> orgJson){
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
				.append(DOC_START_USER, user)
				.append(DOC_END_DATE, -1L)
				.append(DOC_HASH, hash)
				//	.append(DOC_BRANCH, branchArr)
				.append(DOC_BRANCH, branch)
				.append(DOC_ORGANIZATION,  orgJson)
				//				.append(DOC_RAW, jsonObject);
				.append(DOC_RAW, raw);
		return doc;
	}

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
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_NODES);
	}

	public Set<String> retrieveAllRelationshipIDs(String project, String branch){
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_RELATIONS);
	}

	public Set<String> retrieveAllViewIDs(String project, String branch){
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_VIEWS);
	}

	@Override
	public GenericStorageResult insertNodeDocument(String project, String branch, String user, Document jsonObject, long time, Vector<KeyValuePair> org) {
		//		String compStr = parser.getNodeComparisonString(jsonObject);

		int hash = parser.getNodeHash(jsonObject);
		Document doc = null;
		GenericStorageResult ret = new GenericStorageResult();
		String uuid = jsonObject.getString(Archimate3Parser.IDENTIFIER_TAG);
		boolean insert = true;
		ret.setStatusInserted();
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		if (insert){
			doc = enrichDocument( jsonObject, branch, user, time, hash, orgJson);
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
	public GenericStorageResult insertRelationDocument(String project, String branch, String user, Document jsonObject, String sourceUUID, Document sourceJson, String targetUUID, Document targetJson, long time, Vector<KeyValuePair> org) {
//		String compStr = parser.getRelationComparisonString(jsonObject);
		int hash = parser.getRelationHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, user, time, hash, orgJson);
		doc.append("sourceUUID", sourceUUID)
		.append("targetUUID", targetUUID);
		//		.append("source", sourceJson)
		//		.append("target", targetJson);
		
		mongo.insertDocument(project, MongoDBAccess.COLLECTION_RELATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		return ret;
	}

	@Override
	public GenericStorageResult insertOrganizationDocument(String project, String branch, String user, Vector<KeyValuePair> level, ArrayList<Document> labelArr, long time) {
//		String compStr = parser.getOrganizationComparisonString(labelArr);
		int hash = parser.getOrganizationHash(labelArr);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(level, branch);
		Document jsonObj = new Document();
		jsonObj.put(DOC_ORGANIZATION_CONTENT, labelArr);
		jsonObj.put(Archimate3Parser.IDENTIFIER_TAG, "id-"+UUID.randomUUID().toString());
		Document doc = enrichDocument( jsonObj,branch, user, time, hash, orgJson);
		mongo.insertDocument(project,MongoDBAccess.COLLECTION_ORGANIZATIONS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		return ret;
	}

	@Override
	public GenericStorageResult insertViewDocument(String project, String branch, String user, Document jsonObject, long time, Vector<KeyValuePair> org) {
//		String compStr = parser.getViewComparisonString(jsonObject);
		int hash = parser.getViewHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		Document doc = enrichDocument( jsonObject,branch, user, time, hash, orgJson);
		mongo.insertDocument(project,MongoDBAccess.COLLECTION_VIEWS, doc);
		ret.setDoc(doc);
		ret.setStatusInserted();
		return ret;
	} 
	
	public HashMap<String,String> getMapping(String project, long time){
		return mongo.getMapping(project, time);
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
	public Document retrieveViewDocument(String project, String branch, String user, long time, Organization org) {
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
	public Document retrieveNodeDocument(String project, String branch, String user, long time, Organization org) {
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
	public Document retrieveRelationDocument(String project, String branch, String user, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_RELATIONS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());
			Document raw = (Document) doc.get("raw");
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
			item.addLeaf(doc.getString(DOC_ID), doc.getString(DOC_ID));
			elem.add(raw);
		}
		Document ret = new Document();
		ret.append(Archimate3Parser.RELATIONSHIP_TAG, elem);
		return ret;
	}

	@Override
	public void retrieveOrganization(String project, String branch, String user, long time, Organization org) {
		FindIterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_ORGANIZATIONS, parser.getType(), time);
		ArrayList<Document> elem = new ArrayList<Document>();
		MongoCursor<Document> it = docs.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			//				LOGGER.severe("next instance: "+doc.toJson().toString());
			String uuid = doc.getString(DOC_ID);
			Document raw = (Document) doc.get("raw");
			ArrayList<Document> label = (ArrayList<Document>) raw.get(DOC_ORGANIZATION_CONTENT);
			ArrayList<Document> orgs = (ArrayList<Document>) doc.get(DOC_ORGANIZATION);
			Organization item = org;
			for(Document oDoc : orgs){
				String lab = oDoc.getString(DOC_ORGANIZATION_LABEL);
				int pos = oDoc.getInteger(DOC_ORGANIZATION_POSITION);
				if(!item.contains(lab)){
					item.addChild(lab, new Organization(lab),pos, uuid);
				} else {
					item.setChildPosition(lab, pos);
				}
				item = item.getChildByName(lab);
				item.setChildPosition(lab, pos);
			}
			item.setLabel(label);
		}		
	}
		
	@Override
	public Document retrieveOrganizationDocument(String project, String branch, String user, long time, Organization org) {
		retrieveOrganization(project, branch, user, time, org);
		Document elem = createBSONFromOrganization(org);
		return elem;
	}

	private Document createBSONFromOrganization(Organization org) {
		Document ret = new Document();
		ArrayList<Document> e ;
		boolean flag = false;
		if(org.getLabel()!=null){
			//			e = 
			ret.put(Archimate3Parser.LABEL_TAG, org.getLabel());
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
			ret.append(Archimate3Parser.ITEM_TAG, list);
			flag= true;
			//			ret.add(e);
		}
		//if (flag) ret.add(e);

		List<Organization> list = org.getChildren();
		list.remove(null);
		if(list != null && !list.isEmpty()){
			e = new ArrayList<Document>();
			for(Organization o : list){
				if(o!=null){
					e.add(createBSONFromOrganization(o));
					//ArrayList<Document> e2 = createBSONFromOrganization(o);
				}
			}
			ret.append(Archimate3Parser.ITEM_TAG, e);
		}
		return ret;
	}

	public void retireNodeDocument(String project, String branch, String user, String uuid, long time){
		retireDocument(project, branch, user, MongoDBAccess.COLLECTION_NODES, uuid, time);
	}
	
	public void retireRelationshipDocument(String project, String branch, String user, String uuid, long time){
		retireDocument(project, branch, user, MongoDBAccess.COLLECTION_RELATIONS, uuid, time);
	}
	
	public void retireViewDocument(String project, String branch, String user, String uuid, long time){
		retireDocument(project, branch, user, MongoDBAccess.COLLECTION_VIEWS, uuid, time);
	}
	
	protected void retireDocument(String project, String branch, String user, String col, String uuid, long time){
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
				append(DOC_RAW+"."+Archimate3Parser.PROPERTIES_TAG+"."+Archimate3Parser.PROPERTY_TAG+".$."+Archimate3Parser.PROPERTY_VALUE_TAG+"."+Archimate3Parser.VALUE_TAG, String.valueOf(time)).
				append(DOC_END_USER, user));
		mongo.retireDocument(project, col, query, set);
	}

	@Override
	public Document retrieveManagementDocument(String project, String branch, String user, long time) {
		Document ret = null;
		Iterable<Document> docs = mongo.retrieveDocument(project, branch, MongoDBAccess.COLLECTION_MANAGEMENT, parser.getType(), time);
		if(docs !=null && docs.iterator()!=null){
			MongoCursor<Document> it = (MongoCursor<Document>) docs.iterator();
			if(it.hasNext()){
				Document doc = it.next();
				ret = (Document) doc.get(DOC_RAW);
//				if(it.hasNext()){
//					LOGGER.severe("Inconsistency with the management documents of project:"+project+"   branch:"+branch+"    user:"+user+"    time:"+time);
//				}
			}
		}
		return ret;
	}

	@Override
	public void retireManagementDocument(String project, String branch, String user, String ref, long time) {
		retireDocument(project, branch, user, MongoDBAccess.COLLECTION_MANAGEMENT, ref, time);
	}

	@Override
	public GenericStorageResult insertManagementDocument(String project, String branch, String user, Document jsonObject, long time,
			Collection<String> ref_elements, Collection<String> ref_relations, Collection<String> ref_views) {
		int hash = parser.getManagmentHash(jsonObject);
		GenericStorageResult ret = new GenericStorageResult();
		//ArrayList<Document> orgJson = createOrganizationPath(org, branch);
		//Document doc = enrichDocument( jsonObject,branch, user, time, hash, null);
		String uuid = jsonObject.getString(Archimate3Parser.IDENTIFIER_TAG);
		String version = jsonObject.getString(Archimate3Parser.VERSION_TAG);
		Document doc = new Document(DOC_NAME, DOC_NAME_NODE)
				.append(DOC_TYPE, parser.getType())
				.append(DOC_ID, uuid)
				.append(DOC_START_DATE, time)
				.append(DOC_START_USER, user)
				.append(DOC_END_DATE, -1L)
				.append(DOC_HASH, hash)
				//	.append(DOC_BRANCH, branchArr)
				.append(DOC_BRANCH, branch)
				//				.append(DOC_RAW, jsonObject);
				.append(DOC_VERSION, version)
				.append(DOC_RAW, jsonObject);
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(ref_elements);
		doc.append(DOC_ELEMENTS_LIST,list);
		list = new ArrayList<String>();
		list.addAll(ref_relations);
		doc.append(DOC_RELATIONS_LIST,list);
		list = new ArrayList<String>();
		list.addAll(ref_views);
		doc.append(DOC_VIEWS_LIST,list);
		mongo.insertDocument(project,MongoDBAccess.COLLECTION_MANAGEMENT, doc);
		
		ret.setDoc(doc);
		ret.setStatusInserted();
		return ret;
	}

	@Override
	public void retireOrganizationDocument(String project, String branch, String user, String ref, long time) {
		retireDocument(project, branch, user, MongoDBAccess.COLLECTION_ORGANIZATIONS, ref, time);
	}

	@Override
	public Set<String> retrieveAllOrganizationIDs(String project, String branch) {
		return mongo.queryDocumentFindAllIds(project, branch, MongoDBAccess.COLLECTION_ORGANIZATIONS);	
	}
	
	@Override
	public Set<String> retrieveFileNodeIDs(String project, String branch, String fileID, String version) {
		return mongo.queryDocumentFindFileIds(project, branch, DOC_ELEMENTS_LIST, fileID, version);	
	}

	@Override
	public Set<String> retrieveFileRelationshipIDs(String project, String branch, String fileID, String version) {
		return mongo.queryDocumentFindFileIds(project, branch, DOC_RELATIONS_LIST, fileID, version);	
	}

	@Override
	public Set<String> retrieveFileViewIDs(String project, String branch, String fileID, String version) {
		return mongo.queryDocumentFindFileIds(project, branch, DOC_VIEWS_LIST, fileID, version);	
	}

	@Override
	public boolean lockBranch(String project, String branch, String user, String model_id, long time) {
		return mongo.queryLockBranch(project,branch, user, model_id, time);
	}

	@Override
	public void releaseBranch(String project, String branch, String user) {
		mongo.queryReleaseBranch(project,branch, user);
	}

	@Override
	public int retrieveModelHash(String project, String branch, String user, String model_id, long time) {
		return mongo.retrieveModelHash(project,branch, user, model_id, time);
	}

	@Override
	public boolean checkModelCommit(String project, String branch, String model_id, String version) {
		return mongo.checkModelCommit(project,branch, model_id, version);
	}

	@Override
	public void insertMapping(String project, long time, HashMap<String, String> map) {
		mongo.insertMapping(project, time, map);
	}

	@Override
	public void retireMapping(String project, long time) {
		BasicDBObject query = new BasicDBObject(DOC_END_DATE,-1);
		BasicDBObject set = new BasicDBObject("$set", new BasicDBObject(DOC_END_DATE, time));
		mongo.retireDocument(project, MongoDBAccess.COLLECTION_MAPPING, query, set);
	}

}
