package org.iea.connector.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.GenericParser;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.parser.storage.GenericStorageResult;
import org.iea.util.KeyValuePair;
import org.iea.util.Organization;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

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

	public Document insertNodeDocument(GenericParser parser, String project, String branch, String user, Document n, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertNodeDocumentManager(project, branch, user, n, time, org);
			} else { 
				if(ret.isStatusUpdated()){
					v.updateNodeDocument(project, branch, user, n, time);
				} else if(ret.isStatusInserted()){
					v.insertNodeDocumentFollower(project, branch, user, n, time);
				}
			}
		}
		return ret.getDoc();
	}

	public Document insertRelationDocument(GenericParser parser, String project, String branch, String user, Document jsonObject, String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertRelationDocumentManager(project, branch, user, jsonObject, sourceUUID, source, targetUUID, target, time, org);
			} else { 
				v.insertRelationDocumentFollower(project, branch, user, jsonObject, sourceUUID, source, targetUUID, target, time);
			}
		}
		return ret.getDoc();
	}

	public Document insertOrganizationDocument(GenericParser parser, String project, String branch, String user,
			Vector<KeyValuePair> level, ArrayList<Document> labelArr, long time) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertOrganizationDocumentManager(project, branch, user,level, labelArr, time);
			}
		}
		return ret.getDoc();
	}

//	public Document insertOrganizationDocument(GenericParser parser, String project, String branch, String user, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time) {
//		GenericStorageResult ret = null;
//		Vector<StorageConnectorContainer> vec = storage.get(parser);
//		for(StorageConnectorContainer v:vec){
//			if(v.isManagingIDs()){
//				ret = v.insertOrganizationsDocumentManager(project, branch, user, organizationsTypeLabel, item, level, value, time);
////			} else { 
////				v.insertRelationDocumentFollower(project, branch, user, uuid, jsonObject, sourceUUID, source, targetUUID, target, time);
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

	public Document insertViewDocument(GenericParser parser, String project, String branch, String user, String uuid, Document jsonObject, long time, Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertViewDocumentManager(project, branch, user, uuid, jsonObject, time, org);
			} else { 
				v.insertViewDocumentFollower(project, branch, user, uuid, jsonObject, time);
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

	public Document retrieveNodeDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveNodeDocumentManager(project, branch, user, time, org);
			}
		}
		return ret;
	}

	public Document retrieveRelationDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveRelationDocumentManager(project, branch, user, time, org);
			}
		}
		return ret;
	}

	public Document retrieveViewDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveViewDocumentManager(project, branch, user, time, org);
			}
		}
		return ret;
	}

	public Document retrieveOrganizationDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveOrganizationDocumentManager(project, branch, user, time, org);
			}
		}
		return ret;
	}

	public Set<String> retrieveAllNodeIDs(GenericParser parser, String project, String branch) {
		Set<String> ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveAllNodeIDsManager(project, branch);
			}
		}
		return ret;
	}

	public Set<String> retrieveAllRelationshipIDs(GenericParser parser, String project, String branch) {
		Set<String> ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveAllRelationshipIDsManager(project, branch);
			}
		}
		return ret;
	}

	public Set<String> retrieveAllViewIDs(GenericParser parser, String project, String branch) {
		Set<String> ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveAllViewIDsManager(project, branch);
			}
		}
		return ret;
	}


	public void retireNodeDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retireNodeDocument(project, branch, user, ref, time);
			}
		}
	}
	public void retireRelationshipDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retireRelationshipDocument(project, branch, user, ref, time);
			}
		}
	}
	public void retireViewDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retireViewDocument(project, branch, user, ref, time);
			}
		}
	}

	public Document insertManagementDocument(GenericParser parser, String project, String branch, String user, Document n, long time,
			Vector<KeyValuePair> org) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertManagementDocumentManager(project, branch, user, n, time, org);
			} else { 
				if(ret.isStatusUpdated()){
					v.updateManagementDocument(project, branch, user, n, time);
				} else if(ret.isStatusInserted()){
					v.insertManagementDocumentFollower(project, branch, user, n, time);
				}
			}
		}
		return ret.getDoc();
	}

	public void retireManagementDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retireManagementDocument(project, branch, user, ref, time);
			}
		}
	}

	public Document retrieveManagementDocument(GenericParser parser, String project, String branch, String user, long time,
			Organization org) {
		Document ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveManagementDocumentManager(project, branch, user, time, org);
			}
		}
		return ret;
	}

	public void retireOrganizationDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retireOrganizationDocument(project, branch, user, ref, time);
			}
		}
	}

	public void retrieveOrganization(GenericParser parser, String project, String branch, String user, long time,
			Organization org) {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				v.retrieveOrganization(project, branch, user, time, org);
			}
		}
	}

	public Set<String> retrieveAllOrganizationIDs(GenericParser parser, String project, String branch) {
		Set<String> ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.retrieveAllOrganizationIDsManager(project, branch);
			}
		}
		return ret;
	}



}
