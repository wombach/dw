package org.iea.connector.parser.storage;

import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import org.bson.Document;
import org.iea.util.KeyValuePair;
import org.iea.util.Organization;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public interface GenericParserStorageConnectorManager {

	public GenericStorageResult insertNodeDocument(String project, String branch, Document n, long time, Vector<KeyValuePair> org);

	public GenericStorageResult insertRelationDocument(String project, String branch, Document jsonObject, 
			String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org) ;

//	public GenericStorageResult insertOrganizationDocument(String project, String branch, JSONObject jsonObject,
//			String refUUID, JSONObject refJson, long time);

	public GenericStorageResult insertOrganizationDocument(String project, String branch, Vector<KeyValuePair> level,
			ArrayList<Document> labelArr, long time);

	public GenericStorageResult insertViewDocument(String project, String branch, Document jsonObject, long time, Vector<KeyValuePair> org);
	
	public void dropDB();
	
	public void dropProject(String project);
	
	public void dropBranch(String project, String branch);

	
	public Document retrieveViewDocument(String project, String branch, long time, Organization org);

	public Document retrieveNodeDocument(String project, String branch, long time, Organization org);

	public Document retrieveRelationDocument(String project, String branch, long time, Organization org);

	public Document retrieveOrganizationDocument(String project, String branch, long time, Organization org);

	public Set<String> retrieveAllNodeIDs(String project, String branch);

	public Set<String> retrieveAllRelationshipIDs(String project, String branch);

	public Set<String> retrieveAllViewIDs(String project, String branch);

	public void retireNodeDocument(String project, String branch, String ref, long time);

	public void retireRelationshipDocument(String project, String branch, String ref, long time);

	public void retireViewDocument(String project, String branch, String ref, long time);


//	public GenericStorageResult insertOrganizationDocument(String project, String branch, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time);

}
