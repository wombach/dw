package org.iea.connector.parser.storage;

import org.bson.Document;
import org.json.JSONObject;

public interface GenericParserStorageConnectorFollower  {

	public void insertNodeDocument(String project, String branch, JSONObject jsonObject, long time) ;
	
	public void insertRelationDocument(String project, String branch, String uuid, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;
	
	public void updateNodeDocument(String project, String branch, JSONObject jsonObject, long time) ;

	public void updateRelationDocument(String project, String branch, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

	public void dropDB();
	
	public void dropProject(String project);
	
	public void dropBranch(String project, String branch);

	public void insertViewDocument(String project, String branch, String uuid, JSONObject jsonObject, long time);

}
