package org.idw.storage.connector;

import java.util.logging.Logger;

import org.bson.Document;
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

	public Document insertNodeDocument(JSONObject jsonObject, long time){
		return connector.insertNodeDocument(jsonObject, time);
	}

	public Document insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time){
		return connector.insertRelationDocument(jsonObject, sourceUUID, targetUUID, time);
	}

	boolean isManagingIDs(){
		return this.managingIDs;
	}
}
