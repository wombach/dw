package org.iea.connector.storage;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.GenericParser;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.parser.storage.GenericStorageResult;
import org.iea.util.KeyValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

/** 
 * StorageFactory class
 *
 * this class keeps together the different storage anegines which are used in the 
 * project. The query language and the query optimization is NOT part of that 
 * class, but has to be outside this class.
 */
public class StorageFactory {
	private final static Logger LOGGER = Logger.getLogger(StorageFactory.class.getName()); 

	private HashMap<GenericParser,Vector<StorageConnectorContainer>> storage = new HashMap<GenericParser,Vector<StorageConnectorContainer>>();

	public StorageFactory(){
	}

	public void registerStorage(GenericParser parser, GenericParserStorageConnector gs, boolean managingIDs)
	 throws StorageRegistrationException {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		if(vec==null){ 
			vec = new Vector<StorageConnectorContainer>();
			storage.put(parser, vec);
		}
		if(managingIDs){
			boolean flag = false;
			for(StorageConnectorContainer t : vec){
				flag = flag || t.isManagingIDs();
			}
			if(flag) throw new StorageRegistrationException();
			vec.add(0, new StorageConnectorContainer(gs, managingIDs) );
		} else vec.add(new StorageConnectorContainer(gs) );
	}

	public Document insertNodeDocument(GenericParser parser, String project, String branch, JSONObject jsonObject, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertNodeDocumentManager(project, branch, jsonObject, time, org);
			} else { 
				if(ret.isStatusUpdated()){
					v.updateNodeDocument(project, branch, jsonObject, time);
				} else if(ret.isStatusInserted()){
					v.insertNodeDocumentFollower(project, branch, jsonObject, time);
				}
			}
		}
		return ret.getDoc();
	}

	public Document insertRelationDocument(GenericParser parser, String project, String branch, String uuid, JSONObject jsonObject, String sourceUUID, JSONObject source, String targetUUID, JSONObject target, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertRelationDocumentManager(project, branch, uuid, jsonObject, sourceUUID, source, targetUUID, target, time, org);
			} else { 
				v.insertRelationDocumentFollower(project, branch, uuid, jsonObject, sourceUUID, source, targetUUID, target, time);
			}
		}
		return ret.getDoc();
	}

	public Document insertOrganizationDocument(Archimate3Parser parser, String project, String branch,
			Vector<KeyValuePair> level, JSONArray labelArr, long time) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertRelationDocumentManager(project, branch,level, labelArr, time);
			}
		}
		return ret.getDoc();
	}

//	public Document insertOrganizationDocument(GenericParser parser, String project, String branch, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time) {
//		GenericStorageResult ret = null;
//		Vector<StorageConnectorContainer> vec = storage.get(parser);
//		for(StorageConnectorContainer v:vec){
//			if(v.isManagingIDs()){
//				ret = v.insertOrganizationsDocumentManager(project, branch, organizationsTypeLabel, item, level, value, time);
////			} else { 
////				v.insertRelationDocumentFollower(project, branch, uuid, jsonObject, sourceUUID, source, targetUUID, target, time);
//			}
//		}
//		return ret.getDoc();
//	} 	
//	
	public void dropDB() {
		Collection<Vector<StorageConnectorContainer>> vec = storage.values();
		for(Vector<StorageConnectorContainer> vv:vec){
			for(StorageConnectorContainer v:vv ){
				v.dropDB();
			}
		}
	}

	public Document insertViewDocument(GenericParser parser, String project, String branch, String uuid, JSONObject jsonObject, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertViewDocumentManager(project, branch, uuid, jsonObject, time, org);
			} else { 
				v.insertViewDocumentFollower(project, branch, uuid, jsonObject, time);
			}
		}
		return ret.getDoc();
	}

	public void dropProject(String project) {
		Collection<Vector<StorageConnectorContainer>> vec = storage.values();
		for(Vector<StorageConnectorContainer> vv:vec){
			for(StorageConnectorContainer v:vv ){
				v.dropProject(project);
			}
		}		
	} 	
	
	public void dropBranch(String project, String branch) {
		Collection<Vector<StorageConnectorContainer>> vec = storage.values();
		for(Vector<StorageConnectorContainer> vv:vec){
			for(StorageConnectorContainer v:vv ){
				v.dropBranch(project, branch);
			}
		}		
	}

	public Document retrieveNodeDocument(GenericParser parser, String project, String branch, long time) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveNodeDocumentManager(project, branch, time);
			}
		}
		return ret;
	}

	public Document retrieveRelationDocument(GenericParser parser, String project, String branch, long time) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveRelationDocumentManager(project, branch, time);
			}
		}
		return ret;
	}

	public Document retrieveViewDocument(GenericParser parser, String project, String branch, long time) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveViewDocumentManager(project, branch, time);
			}
		}
		return ret;
	}



}
