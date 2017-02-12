package org.idw.storage.connector;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.BSONObject;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

public class Archimate3Neo4jConnector extends GenericParserStorageConnector 
   implements GenericParserStorageConnectorFollower {
	private final static Logger LOGGER = Logger.getLogger(Archimate3Neo4jConnector.class.getName());

	public final static int PRETTY_PRINT_INDENT_FACTOR = 4;
	public final static String DOC_NAME = "name";
	public final static String DOC_NAME_NODE = "node";
	public final static String DOC_TYPE = "type";
	public final static String DOC_START_DATE = "start_date";
	public final static String DOC_END_DATE = "end_date";
	public final static String DOC_RAW = "raw";
	public final static String DOC_RAW_ELEMENT = "element";
	public final static String DOC_ID = "id";
	public final static String DOC_COMPARISON_STRING = "comparison_string";
	public static final String DOC_HASH = "hash";

	
	protected String getNodeComparisonString(JSONObject jsonObject) {
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String node_type = jsonObject.getString("type");
		return parser.type+"|"+node_type+"|"+name.toString();
	}

	protected int getNodeHash(JSONObject jsonObject) {
		BSONObject jsonDoc = (BSONObject)com.mongodb.util.JSON.parse(jsonObject.toString());
		jsonDoc.removeField("identifier");
		return jsonDoc.hashCode();
	}

	/**
	 * {name: {name}, title: {title}})
	 * @param jsonObject
	 * @param time
	 * @return
	 */
	protected String createNodeCreationQuery(JSONObject jsonObject, String type, long time){
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String uuid = jsonObject.getString("identifier");
		String node_type = jsonObject.getString("type");
		String ret = "CREATE (n:"+parser.type+":"+parser.type+"_"+type+":"+node_type+
					"{name:'"+name.toString()+"', "+
					"identifier:'"+uuid+"', "+
				    "modelType:'"+parser.getType()+"', "+
					"nodeType:'"+node_type+"',"+
					"startDate:"+time+", "+
					"endDate: -1 }"+
					")";
		return ret;
	}

	protected String createNodeUpdateQuery(JSONObject jsonObject, String type, long time){
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String uuid = jsonObject.getString("identifier");
		String node_type = jsonObject.getString("type");
		String ret = "CREATE (n:"+parser.type+":"+parser.type+"_"+type+":"+node_type+
					"{name:'"+name.toString()+"' , "+
					"identifier:'"+uuid+"', "+
				    "modelType:'"+parser.getType()+"', "+
					"nodeType:'"+node_type+"',"+
					"startDate:"+time+", "+
					"endDate: -1 }"+
					")";
		return ret;
	}

	@Override
	public void insertNodeDocument(JSONObject jsonObject, long time) {
		String compStr = getNodeComparisonString(jsonObject);
		Neo4jAccess graph = UIControl.getGraph();
		String query = createNodeCreationQuery(jsonObject, "node", time);
		graph.insertNode(query);
	}

	protected String getRelationComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		String compStr = getRelationComparisonString(jsonObject);
		int hash = getRelationHash(jsonObject);
		Neo4jAccess graph = UIControl.getGraph();
		String nodeQuery = createNodeCreationQuery(jsonObject, "relation", time);
		graph.insertNode(nodeQuery);
		String query = createRelationCreationQuery(jsonObject, sourceUUID, targetUUID, time);
		graph.insertRelation(query);
		
		// missing handling of updates
	}

	protected String createRelationCreationQuery(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		/**
		* create a relationship
		* MATCH (a:Person),(b:Person)
		* WHERE a.name = 'Node A' AND b.name = 'Node B'
		* CREATE (a)-[r:RELTYPE { name: a.name + '<->' + b.name }]->(b)
		* RETURN r
		*/
		JSONObject nameObj = jsonObject.getJSONArray("name").getJSONObject(0);
		String name = nameObj.getString("value");
		String uuid = jsonObject.getString("identifier");
		String rel_type = jsonObject.getString("type");
		int rel_weight = Archimate3Relationships.getWeight(rel_type);
		String query = "MATCH (s), (t)," +
				"(rn:"+parser.getType()+"_relation) "+
				"WHERE s.identifier = '"+sourceUUID+"' AND t.identifier = '"+targetUUID+"' "+
				"  AND  rn.identifier = '"+uuid+"' " +
				" AND (s:"+parser.getType()+"_node OR s:"+parser.getType()+"_relation) " +
				" AND (t:"+parser.getType()+"_node OR t:"+parser.getType()+"_relation) " +
				"CREATE r = (s)-[r1:"+parser.getType()+"_is_source] ->(rn)  "+
				" -[r2:"+parser.getType()+"_has_target] -> (t) return r;";
//		String query = "MATCH (s:archimate3_node),(rn:archimate3_relation) "+
//				"WHERE s.identifier = '"+sourceUUID+"' AND  rn.identifier = '"+uuid+"' " +
//				"CREATE (s)-[r:"+parser.getType()+"_is_source] ->(rn) return r;";
		return query;
	}

	private int getRelationHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void updateNodeDocument(JSONObject jsonObject, long time) {
		// TODO Auto-generated method stub
		String query = createNodeUpdateQuery(jsonObject, "node", time);
		LOGGER.severe("implement update node");
	}

	@Override
	public void updateRelationDocument(JSONObject jsonObject, String sourceUUID, String targetUUID, long time) {
		// TODO Auto-generated method stub
		
	} 
}
