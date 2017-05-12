package org.iea.connector.parser.storage;

import org.bson.Document;
import org.json.JSONObject;

public interface GenericParserStorageConnectorManager {

	public GenericStorageResult insertNodeDocument(JSONObject jsonObject, long time);

	public GenericStorageResult insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) ;

}
