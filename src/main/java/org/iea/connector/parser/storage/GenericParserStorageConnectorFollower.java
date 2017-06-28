package org.iea.connector.parser.storage;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public interface GenericParserStorageConnectorFollower  {

	public void insertNodeDocument(String project, String branch, String user, Document n, long time) ;
	
	public void insertRelationDocument(String project, String branch, String user, Document jsonObject, String sourceUUID, Document source, String targetUUID2, Document target, long time) ;
	
	public void insertOrganizationDocument(String project, String branch, String user, String uuid, JSONObject jsonObject, long time);

	public void insertViewDocument(String project, String branch, String user, String uuid, Document jsonObject, long time);
	
	public void updateNodeDocument(String project, String branch, String user, Document n, long time) ;

	public void updateRelationDocument(String project, String branch, String user, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

	public void dropDB();
	
	public void dropProject(String project);
	
	public void dropBranch(String project, String branch);

	public void updateManagementDocument(String project, String branch, String user, Document n, long time);

	public void insertManagementDocument(String project, String branch, String user, Document n, long time);


}
