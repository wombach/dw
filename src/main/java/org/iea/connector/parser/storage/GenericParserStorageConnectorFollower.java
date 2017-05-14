package org.iea.connector.parser.storage;

import org.bson.Document;
import org.json.JSONObject;

public interface GenericParserStorageConnectorFollower  {

	public void insertNodeDocument( JSONObject jsonObject, long time) ;
	
	public void insertRelationDocument(String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;
	
	public void updateNodeDocument(JSONObject jsonObject, long time) ;

	public void updateRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

	public void dropDB();

	public void insertViewDocument(String uuid, JSONObject jsonObject, long time);

}
