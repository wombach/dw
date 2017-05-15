package org.iea.connector.parser.storage;

import org.bson.Document;
import org.json.JSONObject;

public interface GenericParserStorageConnectorManager {

	public GenericStorageResult insertNodeDocument(String project, String branch, JSONObject jsonObject, long time);

	public GenericStorageResult insertRelationDocument(String project, String branch, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

	public void dropDB();
	
	public void dropProject(String project);
	
	public void dropBranch(String project, String branch);

	public GenericStorageResult insertViewDocument(String project, String branch, JSONObject jsonObject, long time);

	public Document retrieveViewDocument(String project, String branch, long time);

	public Document retrieveNodeDocument(String project, String branch, long time);

	public Document retrieveRelationDocument(String project, String branch, long time);

}
