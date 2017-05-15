package org.iea.connector.storage;

import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.parser.storage.GenericParserStorageConnectorFollower;
import org.iea.connector.parser.storage.GenericParserStorageConnectorManager;
import org.iea.connector.parser.storage.GenericStorageResult;
import org.json.JSONObject;

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

	public GenericStorageResult insertNodeDocumentManager(String project, String branch, JSONObject jsonObject, long time){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertNodeDocument(project, branch, jsonObject, time);
		} 
		return null;
	}

	public void insertNodeDocumentFollower(String project, String branch, JSONObject jsonObject, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertNodeDocument(project, branch, jsonObject, time);
		} 
	}

	public GenericStorageResult insertRelationDocumentManager(String project, String branch, String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertRelationDocument(project, branch, jsonObject, sourceUUID, targetUUID, time);
		} 
		return null;
	}

	public void insertRelationDocumentFollower(String project, String branch, String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertRelationDocument(project, branch, uuid, jsonObject, sourceUUID, targetUUID, time);
		} 
	}

	boolean isManagingIDs(){
		return this.managingIDs;
	}

	public void updateNodeDocument(String project, String branch, JSONObject jsonObject, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).updateNodeDocument(project, branch, jsonObject, time);
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

	public GenericStorageResult insertViewDocumentManager(String project, String branch, String uuid, JSONObject jsonObject, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertViewDocument(project, branch, jsonObject, time);
		} 
		return null;
	}

	public void insertViewDocumentFollower(String project, String branch, String uuid, JSONObject jsonObject, long time) {
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
}
