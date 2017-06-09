package org.iea.connector.parser.storage;

import java.util.Vector;

import org.bson.Document;
import org.iea.util.KeyValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

public interface GenericParserStorageConnectorManager {

	public GenericStorageResult insertNodeDocument(String project, String branch, JSONObject jsonObject, long time, Vector<KeyValuePair> org);

	public GenericStorageResult insertRelationDocument(String project, String branch, JSONObject jsonObject, 
			String sourceUUID, JSONObject source, String targetUUID, JSONObject target, long time, Vector<KeyValuePair> org) ;

//	public GenericStorageResult insertOrganizationDocument(String project, String branch, JSONObject jsonObject,
//			String refUUID, JSONObject refJson, long time);

	public GenericStorageResult insertOrganizationDocument(String project, String branch, Vector<KeyValuePair> level,
			JSONArray labelArr, long time);

	public GenericStorageResult insertViewDocument(String project, String branch, JSONObject jsonObject, long time, Vector<KeyValuePair> org);
	
	public void dropDB();
	
	public void dropProject(String project);
	
	public void dropBranch(String project, String branch);

	
	public Document retrieveViewDocument(String project, String branch, long time);

	public Document retrieveNodeDocument(String project, String branch, long time);

	public Document retrieveRelationDocument(String project, String branch, long time);


//	public GenericStorageResult insertOrganizationDocument(String project, String branch, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time);

}
