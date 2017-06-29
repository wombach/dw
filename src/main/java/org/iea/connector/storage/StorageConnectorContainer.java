package org.iea.connector.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.parser.storage.GenericParserStorageConnectorFollower;
import org.iea.connector.parser.storage.GenericParserStorageConnectorManager;
import org.iea.connector.parser.storage.GenericStorageResult;
import org.iea.util.KeyValuePair;
import org.iea.util.Organization;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

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

	public GenericStorageResult insertNodeDocumentManager(String project, String branch, String user, Document n, long time, Vector<KeyValuePair> org){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertNodeDocument(project, branch, user, n, time, org);
		} 
		return null;
	}

	public void insertNodeDocumentFollower(String project, String branch, String user, Document n, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertNodeDocument(project, branch, user, n, time);
		} 
	}

	public GenericStorageResult insertRelationDocumentManager(String project, String branch, String user,  Document jsonObject, String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org){
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertRelationDocument(project, branch, user, jsonObject, sourceUUID, source, targetUUID, target, time, org);
		} 
		return null;
	}

	public void insertRelationDocumentFollower(String project, String branch, String user, Document jsonObject, String sourceUUID, Document source, String targetUUID, Document target, long time){
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertRelationDocument(project, branch, user, jsonObject, sourceUUID, source,  targetUUID, target, time);
		} 
	}

	boolean isManagingIDs(){
		return this.managingIDs;
	}

	public void updateNodeDocument(String project, String branch, String user, Document n, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).updateNodeDocument(project, branch, user, n, time);
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

	public GenericStorageResult insertViewDocumentManager(String project, String branch, String user, String uuid, Document jsonObject, long time, Vector<KeyValuePair> org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertViewDocument(project, branch, user, jsonObject, time, org);
		} 
		return null;
	}

	public void insertViewDocumentFollower(String project, String branch, String user, String uuid, Document jsonObject, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertViewDocument(project, branch, user, uuid, jsonObject, time);
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

	public Document retrieveViewDocumentManager(String project, String branch, String user, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveViewDocument(project, branch, user, time, org);
		}
		return null;
	}

	public Document retrieveNodeDocumentManager(String project, String branch, String user, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveNodeDocument(project, branch, user, time, org);
		}
		return null;
	}

	public Document retrieveRelationDocumentManager(String project, String branch, String user, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveRelationDocument(project, branch, user, time, org);
		}
		return null;
	}

	public GenericStorageResult insertOrganizationDocumentManager(String project, String branch, String user, Vector<KeyValuePair> level,
			ArrayList<Document> labelArr, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertOrganizationDocument(project, branch, user,level, labelArr,  time);
		}
//		return null;
		return null;
	}

	public Document retrieveOrganizationDocumentManager(String project, String branch, String user, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveOrganizationDocument(project, branch, user, time, org);
		}
		return null;
	}

	public Set<String> retrieveAllNodeIDsManager(String project, String branch) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveAllNodeIDs(project, branch);
		}
		return null;
	}

	public Set<String> retrieveAllRelationshipIDsManager(String project, String branch) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveAllRelationshipIDs(project, branch);
		}
		return null;
	}

	public Set<String> retrieveAllViewIDsManager(String project, String branch) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveAllViewIDs(project, branch);
		}
		return null;
	}

	public void retireNodeDocument(String project, String branch, String user, String ref, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retireNodeDocument(project, branch, user, ref, time);
		}
	}

	public void retireRelationshipDocument(String project, String branch, String user, String ref, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retireRelationshipDocument(project, branch, user, ref, time);
		}		
	}

	public void retireViewDocument(String project, String branch, String user, String ref, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retireViewDocument(project, branch, user, ref, time);
		}		
	}

	public void retireManagementDocument(String project, String branch, String user, String ref, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retireManagementDocument(project, branch, user, ref, time);
		}
	}

	public GenericStorageResult insertManagementDocumentManager(String project, String branch, String user, Document n, long time,
			Collection<String> ref_elements, Collection<String> ref_relations, Collection<String> ref_views) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).insertManagementDocument(project, branch, user, n, time, 
					ref_elements, ref_relations, ref_views);
		} 
		return null;
	}

	public void updateManagementDocument(String project, String branch, String user, Document n, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).updateManagementDocument(project, branch, user, n, time);
		} 	
	}

	public void insertManagementDocumentFollower(String project, String branch, String user, Document n, long time) {
		if(GenericParserStorageConnectorFollower.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorFollower) connector).insertManagementDocument(project, branch, user, n, time);
		} 
	}

	public Document retrieveManagementDocumentManager(String project, String branch, String user, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveManagementDocument(project, branch, user, time);
		}
		return null;
	}

	public void retireOrganizationDocument(String project, String branch, String user, String ref, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retireOrganizationDocument(project, branch, user, ref, time);
		}
	}

	public void retrieveOrganization(String project, String branch, String user, long time, Organization org) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).retrieveOrganization(project, branch, user, time, org);
		}		
	}

	public Set<String> retrieveAllOrganizationIDsManager(String project, String branch) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveAllOrganizationIDs(project, branch);
		}
		return null;
	}

	public Set<String> retrieveFileNodeIDsManager(String project, String branch, String fileID, String version) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveFileNodeIDs(project, branch, fileID, version);
		}
		return null;
	}

	public Set<String> retrieveFileRelationshipIDsManager(String project, String branch, String fileID, String version) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveFileRelationshipIDs(project, branch, fileID, version);
		}
		return null;
	}

	public Set<String> retrieveFileViewIDsManager(String project, String branch, String fileID, String version) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveFileViewIDs(project, branch, fileID, version);
		}
		return null;
	}

//	public Set<String> retrieveFileOrganizationIDsManager(String project, String branch, String fileID) {
//		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
//			return ((GenericParserStorageConnectorManager) connector).retrieveFileOrganizationIDs(project, branch, fileID);
//		}
//		return null;
//	}

	public boolean lockBranchManager(String project, String branch, String user, String model_id, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).lockBranch(project, branch, user, model_id, time);
		}
		return false;
	}

	public int retrieveModelHashManager(String project, String branch, String user, String model_id, long time) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).retrieveModelHash(project, branch, user, model_id, time);
		}
		return 0;
	}

	public void releaseBranchManager(String project, String branch, String user) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			((GenericParserStorageConnectorManager) connector).releaseBranch(project, branch, user);
		}
	}

	public boolean checkModelCommit(String project, String branch, String model_id, String version) {
		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
			return ((GenericParserStorageConnectorManager) connector).checkModelCommit(project, branch, model_id, version);
		}
		return false;
	}


//	public GenericStorageResult insertOrganizationsDocumentManager(String project, String branch, String user,
//			String organizationsTypeLabel, JSONObject item, Vector<String> level, String value, long time) {
//		if(GenericParserStorageConnectorManager.class.isAssignableFrom(connector.getClass())){
//			return ((GenericParserStorageConnectorManager) connector).insertOrganizationDocument(project, branch, user,organizationsTypeLabel, item, level, value, time);
//		}
//		return null;
//	}
}
