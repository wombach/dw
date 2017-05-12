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

	public GenericStorageResult insertNodeDocumentManager(JSONObject jsonObject, long time){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertNodeDocument(jsonObject, time);
		} 
		return null;
	}

	public void insertNodeDocumentFollower(JSONObject jsonObject, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertNodeDocument(jsonObject, time);
		} 
	}

	public GenericStorageResult insertRelationDocumentManager(String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertRelationDocument(jsonObject, sourceUUID, targetUUID, time);
		} 
		return null;
	}

	public void insertRelationDocumentFollower(String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertRelationDocument(uuid, jsonObject, sourceUUID, targetUUID, time);
		} 
	}

	boolean isManagingIDs(){
		return this.managingIDs;
	}

	public void updateNodeDocument(JSONObject jsonObject, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).updateNodeDocument(jsonObject, time);
		} 
	}
}
