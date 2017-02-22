package org.idw.storage.connector;

import org.bson.Document;
import org.json.JSONObject;

public interface GenericParserStorageConnectorFollower  {

	public void insertNodeDocument(JSONObject jsonObject, long time) ;
	
	public void insertRelationDocument(String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;
	
	public void updateNodeDocument(JSONObject jsonObject, long time) ;

	public void updateRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

}
