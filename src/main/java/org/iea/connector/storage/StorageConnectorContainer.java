package org.iea.connector.storage;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.parser.storage.GenericParserStorageConnectorFollower;
import org.iea.connector.parser.storage.GenericParserStorageConnectorManager;
import org.iea.connector.parser.storage.GenericStorageResult;
import org.iea.util.KeyValuePair;
import org.iea.util.Organization;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public class StorageConnectorContainer {

	private final static Logger LOGGER = Logger.getLogger(StorageConnectorContainer.class.getName()); 
	private boolean managingIDs = false;
	private GenericParserStorageConnector connector;

	public StorageConnectorContainer(GenericParserStorageConnector connector){
		this.connector = connector;
	}

	public StorageConnectorContainer(GenericParserStorageConnector connector, boolean managingIDs){
		this.connector = connector;
		this.managingIDs = managingIDs;
	}

	public GenericStorageResult insertNodeDocumentManager(String project, String branch, Document n, long time, Vector<KeyValuePair> org){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertNodeDocument(project, branch, n, time, org);
		} 
		return null;
	}

	public void insertNodeDocumentFollower(String project, String branch, Document n, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertNodeDocument(project, branch, n, time);
		} 
	}

	public GenericStorageResult insertRelationDocumentManager(String project, String branch,  Document jsonObject, String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertRelationDocument(project, branch, jsonObject, sourceUUID, source, targetUUID, target, time, org);
		} 
		return null;
	}

	public void insertRelationDocumentFollower(String project, String branch, Document jsonObject, String sourceUUID, Document source, String targetUUID, Document target, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertRelationDocument(project, branch, jsonObject, sourceUUID, source,  targetUUID, target, time);
		} 
	}

	boolean isManagingIDs(){
		return this.managingIDs;
	}

	public void updateNodeDocument(String project, String branch, Document n, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).updateNodeDocument(project, branch, n, time);
		} 
	}

	public void dropDB() {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).dropDB();
		} else 
			if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
				((GenericParserStorageConnectorManager) connector).dropDB();
			} 
			
	}

	public GenericStorageResult insertViewDocumentManager(String project, String branch, String uuid, Document jsonObject, long time, Vector<KeyValuePair> org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertViewDocument(project, branch, jsonObject, time, org);
		} 
		return null;
	}

	public void insertViewDocumentFollower(String project, String branch, String uuid, Document jsonObject, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertViewDocument(project, branch, uuid, jsonObject, time);
		} 
	}

	public void dropProject(String project) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).dropProject(project);
		} else 
			if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
				((GenericParserStorageConnectorManager) connector).dropProject(project);
			} 		
	}

	public void dropBranch(String project, String branch) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).dropBranch(project, branch);
		} else 
			if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
				((GenericParserStorageConnectorManager) connector).dropBranch(project, branch);
			} 		
	}

	public Document retrieveViewDocumentManager(String project, String branch, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveViewDocument(project, branch, time, org);
		}
		return null;
	}

	public Document retrieveNodeDocumentManager(String project, String branch, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveNodeDocument(project, branch, time, org);
		}
		return null;
	}

	public Document retrieveRelationDocumentManager(String project, String branch, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveRelationDocument(project, branch, time, org);
		}
		return null;
	}

	public GenericStorageResult insertRelationDocumentManager(String project, String branch, Vector<KeyValuePair> level,
			ArrayList<Document> labelArr, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertOrganizationDocument(project, branch,level, labelArr,  time);
		}
//		return null;
		return null;
	}

	public Document retrieveOrganizationDocumentManager(String project, String branch, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveOrganizationDocument(project, branch, time, org);
		}
		return null;
	}

//	public GenericStorageResult insertOrganizationsDocumentManager(String project, String branch,
//			String organizationsTypeLabel, JSONObject item, Vector<String> level, String value, long time) {
//		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
//			return ((GenericParserStorageConnectorManager) connector).insertOrganizationDocument(project, branch,organizationsTypeLabel, item, level, value, time);
//		}
//		return null;
//	}
}
