package org.idw.storage.connector;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONObject;

/** 
 * StorageFactory class
 *
 * this class keeps together the different storage anegines which are used in the 
 * project. The query language and the query optimization is NOT part of that 
 * class, but has to be outside this class.
 */
public class StorageFactory {
	private final static Logger LOGGER = Logger.getLogger(StorageFactory.class.getName()); 

	private HashMap<GenericParser,Vector<StorageConnectorContainer>> storage = new HashMap<GenericParser,Vector<StorageConnectorContainer>>();

	public StorageFactory(){
	}

	public void registerStorage(GenericParser parser, GenericParserStorageConnector gs, boolean managingIDs)
	 throws StorageRegistrationException {
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		if(vec==null){ 
			vec = new Vector<StorageConnectorContainer>();
			storage.put(parser, vec);
		}
		if(managingIDs){
			boolean flag = false;
			for(StorageConnectorContainer t : vec){
				flag = flag || t.isManagingIDs();
			}
			if(flag) throw new StorageRegistrationException();
			vec.add(0, new StorageConnectorContainer(gs, managingIDs) );
		} else vec.add(new StorageConnectorContainer(gs) );
	}

	public Document insertNodeDocument(GenericParser parser, JSONObject jsonObject, long time) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertNodeDocumentManager(jsonObject, time);
			} else { 
				if(ret.isStatusUpdated()){
					v.updateNodeDocument(jsonObject, time);
				} else if(ret.isStatusInserted()){
					v.insertNodeDocumentFollower(jsonObject, time);
				}
			}
		}
		return ret.getDoc();
	}

	public Document insertRelationDocument(GenericParser parser, JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		GenericStorageResult ret = null;
		Vector<StorageConnectorContainer> vec = storage.get(parser);
		for(StorageConnectorContainer v:vec){
			if(v.isManagingIDs()){
				ret = v.insertRelationDocumentManager(jsonObject, sourceUUID, targetUUID, time);
			} else { 
				v.insertRelationDocumentFollower(jsonObject, sourceUUID, targetUUID, time);
			}
		}
		return ret.getDoc();
	} 	
	
}
