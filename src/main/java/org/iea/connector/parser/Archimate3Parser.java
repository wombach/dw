package org.iea.connector.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.bson.BSONObject;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.iea.connector.storage.MongoDBAccess;
import org.iea.pool.TaskState;
import org.iea.pool.TaskStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.xsd.archimate._3.ModelType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import org.iea.util.KeyValuePair;
import org.iea.util.Organization;

public class Archimate3Parser extends GenericParser {

	private final static Logger LOGGER = Logger.getLogger(Archimate3Parser.class.getName());
	public final static String URI = "http://www.opengroup.org/xsd/archimate/3.0/";
	public final static String IDENTIFIER_TAG = "@identifier";
	public final static String MODEL_TAG = "ar3_model";
	public final static String ELEMENTS_TAG = "ar3_elements";
	public final static String ELEMENT_TAG = "ar3_element";
	public final static String RELATIONSHIPS_TAG = "ar3_relationships";
	public final static String RELATIONSHIP_TAG = "ar3_relationship";
	public final static String VIEWS_TAG = "ar3_views";
	public final static String DIAGRAMS_TAG = "ar3_diagrams";
	public final static String VIEW_TAG = "ar3_view";
	public final static String SOURCE_TAG = "@source";
	public final static String TARGET_TAG = "@target";
	public final static String TYPE_TAG = "@xsi_type";
	public final static String CONNECTION_TAG = "ar3_connection";
	public final static String RELATIONSHIPREF_TAG = "@relationshipRef";
	public final static String NODES_TAG = "ar3_node";
	public final static String ELEMENTREF_TAG = "@elementRef";
	public final static String NAME_TAG = "ar3_name";
	public final static String VALUE_TAG = "value";
	public final static String ORGANIZATIONS_TAG = "ar3_organizations";
	public final static String ORGANIZATIONS_TYPE_LABEL = "label";
	public final static String ORGANIZATIONS_TYPE_IDENTIFIERREF = "@identifierRef";
	public final static String ITEM_TAG = "ar3_item";
	public final static String LABEL_TAG = "ar3_label";
	public final static String IDENTIFIERREF_TAG = "@identifierRef";
	public static final String ORGANIZATION_JSON_LABEL = "organization_label";
	public static final String ORGANIZATION_JSON_POSITION = "organization_position";
	public static final String PROPERTIES_TAG = "ar3_properties";
	public static final String PROPERTY_TAG = "ar3_property";
	public static final String PROPERTY_DEFINITIONS_TAG = "ar3_propertyDefinitions";
	public static final String PROPERTY_DEFINITION_TAG = "ar3_propertyDefinition";
	public static final String PROPERTY_DEFINITIONREF_TAG = "@propertyDefinitionRef";
	public static final String PROPERTY_VALUE_TAG = "ar3_value";
	public static final String PROPERTY_DEFINITION_TYPE_TAG = "@type";
	public static final String VERSION_TAG = "@version";
	private static final String DEFAULT_USER = "default";


	public Archimate3Parser(){
		this.type = "archimate3";
		this.CONTEXT = "org.opengroup.xsd.archimate._3";
		this.MODEL_CLASS = ModelType.class;
	}


//	private void addPropertyStandardTypeDefs(JSONObject jobj) {
//		/**
//		 * "propertyDefinitions": {"propertyDefinitions": [
//        {
//            "identifier": "propid-junctionType",
//            "name": "JunctionType",
//            "type": "string"
//        },
//		 */
//		JSONObject defs = null;
//		if(jobj.has("propertyDefinitions")){
//			defs = jobj.getJSONObject("propertyDefinitions");
//		} else {
//			defs = new JSONObject();
//			jobj.append("propertyDefinitions", defs);
//		}
//		JSONArray def = null;
//		boolean flag = false;
//		if(defs.has("propertyDefinition")){
//			Object obj = defs.get("propertyDefinition");
//			if(obj instanceof JSONObject){
//				// if it is a single object it can not our definitions
//				def = new JSONArray();
//				def.put((JSONObject) obj);
//				defs.append("propertyDefinition", def);
//				flag = true;
//			} else if(obj instanceof JSONArray){
//				def = (JSONArray) obj;
//				for(int i=0;i<def.length();i++){
//					if(def.getJSONObject(i).get("identifier").toString().startsWith("propid_iea_")){
//						flag = true;
//						break;
//					}
//				}
//			} else LOGGER.severe("Wrong object type!");
//		} 
//		if(!flag){
//			defs.append("propertyDefinition", new JSONObject(" {\"identifier\": "+
//					"\"propidIEAStartDate\", "+
//					//					"\"propid_iea_start_date\", "+
//					" \"type\": \"number\" }"));
//			//		"\"name\": \"iea start date\", \"type\": \"number\" }"));
//			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
//					"\"propidIEAEndDate\", "+
//					//					"\"propid_iea_end_date\", "+
//					" 'type': number }"));
//			//		"\"name\": \"iea end date\", \"type\": \"number\" }"));
//			defs.append("propertyDefinition", new JSONObject(" {'identifier': "+
//					"\"propidIEAIdentifier\", "+
//					//					"\"propid_iea_identifier\", "+
//					" 'type': string }"));
//			//		"\"name\": \"iea identifier\", \"type\": \"string\" }"));
//		}
//	}

	@Override
	public int getNodeHash(Document jsonObject) {
		return jsonObject.hashCode();
	}

	@Override
	public int getManagmentHash(Document jsonObject) {
		return jsonObject.hashCode();
	}

	@Override
	public int getRelationHash(Document jsonObject) {
		return jsonObject.hashCode();
	}

	@Override
	public int getViewHash(Document jsonObject) {
		return jsonObject.hashCode();
	}

	protected Object getPropid(Document props, String propid){
		Object ret = null;
		ArrayList<Document> prop = (ArrayList<Document>) props.get(PROPERTY_TAG);
		for(Document d : prop){
			String pid = d.getString(PROPERTY_DEFINITIONREF_TAG);
			if(pid.equals(propid)){
				Document val = (Document) d.get(PROPERTY_VALUE_TAG);
				ret = val.get(VALUE_TAG);
				//				ret = v.get(propid);
				break;
			}
		}
		return ret;
	}

	@Override
	public boolean processJsonString(String taskId, String project, String branch, String user, String str, boolean overwrite) {
		boolean ret = false;
		
		HashMap<String,String> map = new HashMap<String,String>();
		//		JSONObject xmlJSONObj = new JSONObject(str);
		Document doc_all = Document.parse(str);
		// check that the file is indeed an archimate file
		long time = System.currentTimeMillis();
		String retMsg = "";
		HashMap<String,Document> nodeMap = new HashMap<String,Document>();
		boolean overall_insert = false;
		boolean overall_update = false;
		Set<String> ref_elements = new HashSet<String>();
		Set<String> ref_relations = new HashSet<String>();
		Set<String> ref_views = new HashSet<String>();
		if( doc_all!=null && doc_all.containsKey(MODEL_TAG)){
			Document obj = (Document) doc_all.get(MODEL_TAG);
			String version = obj.getString(VERSION_TAG);
			String model_id = obj.getString(IDENTIFIER_TAG);
			// check whether document version is up to date and try to lock the document for commit
			// locking is done on branch level!
			boolean lock_successfull = factory.lockBranch(this, project, branch, user, model_id, time);
			if(lock_successfull){
				boolean commit = factory.checkModelCommit(this, project, branch, model_id, version);
				if(commit){
					int model_hash_old = factory.retrieveModelHash(this, project, branch, user, model_id, time);

					//			String xmlns = obj.getString("xmlns");
					//			if (xmlns!=null && !xmlns.isEmpty() && xmlns.equals(URI)) {
					ret = true;
					// organizations
					ArrayList<Document> orgs = (ArrayList<Document>) obj.remove(ORGANIZATIONS_TAG);
					// call a recursion function to parse the tree and add one document per element into the organizations collection
					HashMap<String, Vector<KeyValuePair>> orgMap = new HashMap<String,Vector<KeyValuePair>>();
					Organization org = new Organization();
					factory.retrieveOrganization(this, project, branch, user, time, org);
					Set<String> refs  = factory.retrieveAllOrganizationIDs(this, project,branch);
					createOrganizationLookup(project, branch, user, orgs, orgMap, time, new Vector<KeyValuePair>(), org,refs, overall_insert, overall_update);
					// delete unreferenced items
					LOGGER.info("#Organizations to be deleted: "+refs.size());
					for(String ref : refs){
						factory.retireOrganizationDocument(this,project, branch, user, ref, time);
					}

					//
					// elements
					refs = factory.retrieveFileNodeIDs(this, project,branch, model_id, version);
					
					Document els = (Document) obj.get(ELEMENTS_TAG);
					ArrayList<Document> l = (ArrayList<Document>) els.get(ELEMENT_TAG);
					LOGGER.info("number of lements: "+l.size());
					els.remove(ELEMENT_TAG);
					for(int i=0;i<l.size();i++){
						boolean insert = true; // false means no action required
						boolean update = false; 
						Document n = l.get(i);
						// need to remove properties for calculating the hash
						String identifier = (String) n.remove(IDENTIFIER_TAG);
						Document props = (Document) n.remove(PROPERTIES_TAG);
						String uuid = "id-"+UUID.randomUUID().toString();
						if(props!=null){
							String old_uuid = (String) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER);
							if(old_uuid!=null && !old_uuid.isEmpty()){
								uuid = old_uuid;
								update = true;
							}
							n.put(IDENTIFIER_TAG, uuid);
							int old_hash = (int) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_HASH);
							//					if(hashStr!=null && !hashStr.isEmpty()){
							//int old_hash = Integer.valueOf(hashStr).intValue(); 
							int hash = getNodeHash(n);
							// it is an update
							if(old_hash==hash){
								insert = false; // no update or insert needed
							}
							//	add properties after calculating the hash				}
							n.put(PROPERTIES_TAG, props);
						} else {
							n.put(IDENTIFIER_TAG, uuid);
						}
						refs.remove(uuid);
						ref_elements.add(uuid);
						if(insert){
							overall_insert = true;
							if(update){ 
								overall_update = true;
								factory.retireNodeDocument(this, project, user, branch, uuid, time);
							}
							factory.insertNodeDocument(this, taskId, project, branch, user, n, time, orgMap.get(identifier));
						}
						//				String uuid = getUUID(doc);
						ArrayList<Document> nameArr = (ArrayList<Document>) n.get("ar3_name");
						Document nameObj = nameArr.get(0);
						String name = nameObj.getString("value");
						map.put(identifier, uuid);
						Document s = new Document();
						s.put("name", name);
						s.put("time", time);
						s.put("type", getType());
						s.put("branch",branch);
						nodeMap.put(uuid, s );
					}
					// delete deleted nodes by deleting the remaining elements in refs
					LOGGER.info("#Elements to be deleted: "+refs.size());
					for(String ref : refs){
						factory.retireNodeDocument(this,project, branch, user, ref, time);
					}

					//
					// relations
					refs = factory.retrieveFileRelationshipIDs(this, project,branch, model_id, version);
					
					Document rels =  (Document) obj.get(RELATIONSHIPS_TAG);
					l = (ArrayList<Document>) rels.get(RELATIONSHIP_TAG);
					LOGGER.info("number of relationships: "+l.size());
					rels.remove(RELATIONSHIP_TAG); 
					Vector<Document> v;
					Vector<Document> v2 = new Vector<Document>();
					//			HashMap<String,String> relIds = new HashMap<String,String>();
					for(int i=0;i<l.size();i++){
						//				boolean insert = true; // false means no action required
						//				boolean update = false; 
						Document rel = l.get(i);
						//				String identifier = rel.getString(IDENTIFIER_TAG);
						String identifier = (String) rel.get(IDENTIFIER_TAG);
						Document props = (Document) rel.remove(PROPERTIES_TAG);
						String uuid = "id-"+UUID.randomUUID().toString();
						if(props!=null){
							String old_uuid = (String) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER);
							if(old_uuid!=null && !old_uuid.isEmpty()){
								uuid = old_uuid;
								//						update = true;
							}
							//rel.put(IDENTIFIER_TAG, uuid);
							//					int old_hash = (int) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_HASH);
							//					//					if(hashStr!=null && !hashStr.isEmpty()){
							//					//int old_hash = Integer.valueOf(hashStr).intValue(); 
							//					int hash = getNodeHash(rel);
							//					// it is an update
							//					if(old_hash==hash){
							//						insert = false; // no update or insert needed
							//					}
							//					//	add properties after calculating the hash				}
							rel.put(PROPERTIES_TAG, props);
							//} else {
							//rel.put(IDENTIFIER_TAG, uuid);
						}
						refs.remove(uuid);
						ref_relations.add(uuid);
						//				if(insert){
						//					if(update) 
						//						factory.retireNodeDocument(this, project, branch, uuid, time);
						//					factory.insertNodeDocument(this, project, branch, rel, time, orgMap.get(identifier));
						//				}
						//				if (identifier.equals("relation-fcdb1ce9-89c7-e611-8309-5ce0c5d8efd6")){
						//					LOGGER.info("found it");
						//				}
						map.put(identifier, uuid);
						v2.add(rel);
					}
					// delete deleted nodes by deleting the remaining elements in refs
					LOGGER.info("#Relationships to be deleted: "+refs.size());
					for(String ref : refs){
						factory.retireRelationshipDocument(this,project, branch, user, ref, time);
					}
					while(!v2.isEmpty()){
						v = v2;
						v2 = new Vector<Document>();
						while(!v.isEmpty()){
							boolean insert = true; // false means no action required
							boolean update = false; 
							int i = (int) (Math.random() * v.size());
							Document rel = v.remove(i);
							String identifier = (String) rel.remove(IDENTIFIER_TAG);
							//					if (identifier.equals("relation-f41438e9-89c7-e611-8309-5ce0c5d8efd6")){
							//						LOGGER.info("found it");
							//					}
							String source = rel.getString(SOURCE_TAG);
							String target = rel.getString(TARGET_TAG);
							String sourceUUID = map.get(source);
							if(sourceUUID==null){
								if(map.containsKey(source)){
									sourceUUID = map.get(source);
								}
							}
							rel.remove(SOURCE_TAG);
							rel.put(SOURCE_TAG, sourceUUID);
							String targetUUID = map.get(target);
							if(targetUUID==null){
								if(map.containsKey(target)){
									targetUUID = map.get(target);
								}
							}
							rel.remove(TARGET_TAG);
							rel.put(TARGET_TAG, targetUUID);
							if (sourceUUID==null || sourceUUID.isEmpty() || 
									targetUUID==null || targetUUID.isEmpty()){
								String type = rel.getString(TYPE_TAG);
								if(sourceUUID == null){
									retMsg += "Relation "+identifier+" ("+type+") related source ID can not be found: "+source+"\n";
								} else if(targetUUID==null){
									retMsg += "Relation "+identifier+" ("+type+") related target ID can not be found: "+target+"\n";
								} 
							} else {
								//						String uuid = map.get(identifier);
								//						rel.put(IDENTIFIER_TAG,uuid);
								//Document doc = factory.insertRelationDocument(this, project, branch, rel, sourceUUID, nodeMap.get(sourceUUID), targetUUID, nodeMap.get(targetUUID), time, orgMap.get(identifier));
								//map.put(identifier, uuid);
								// handle insert/updates/do nothing
								rel.put(IDENTIFIER_TAG, map.get(identifier));
								Document props = (Document) rel.remove(PROPERTIES_TAG);
								if(props!=null){
									String old_uuid = (String) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER);
									if(old_uuid!=null && !old_uuid.isEmpty()){
										update = true;
									}
									int old_hash = (int) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_HASH);
									//					if(hashStr!=null && !hashStr.isEmpty()){
									//int old_hash = Integer.valueOf(hashStr).intValue(); 
									int hash = getRelationHash(rel);
									// it is an update
									if(old_hash==hash){
										insert = false; // no update or insert needed
									}
									//	add properties after calculating the hash				}
									rel.put(PROPERTIES_TAG, props);
								}
								if(insert){
									overall_insert = true;
									if(update){ 
										overall_update = true;
										factory.retireRelationshipDocument(this, project, branch, user, identifier, time);
									}
									//factory.insertNodeDocument(this, project, branch, rel, time, orgMap.get(identifier));
									factory.insertRelationDocument(this, taskId, project, branch, user, rel, sourceUUID, nodeMap.get(sourceUUID), targetUUID, nodeMap.get(targetUUID), time, orgMap.get(identifier));
								}
							}
						}
					}

					// views
					if(obj.containsKey(VIEWS_TAG)){
						refs = factory.retrieveFileViewIDs(this, project,branch, model_id, version);
						
						Document views =  (Document) obj.get(VIEWS_TAG);
						Document diags = (Document) views.get(DIAGRAMS_TAG);
						//			for( int ii=0;ii<diags.length();ii++){
						ArrayList<Document> lo = (ArrayList<Document>) diags.remove(VIEW_TAG);
						LOGGER.info("number of views: "+lo.size());
						for( int ii=0;ii<lo.size();ii++){
							int cnt=0;
							Document view = lo.get(ii);
							String view_id = (String) view.remove(IDENTIFIER_TAG);
							String id;
							String uuid = null; //"id-"+UUID.randomUUID().toString();
							boolean insert = true; // false means no action required
							boolean update = false; 
							Document props = (Document) view.remove(PROPERTIES_TAG);
							String view_uuid = "id-"+UUID.randomUUID().toString();
							int old_hash = 0;
							if(props!=null){
								String old_uuid = (String) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER);
								if(old_uuid!=null && !old_uuid.isEmpty()){
									view_uuid = old_uuid;
									update = true;
								}
								old_hash = (int) getPropid(props, Archimate3MongoDBConnector.PROPID_IEA_HASH);
								//					if(hashStr!=null && !hashStr.isEmpty()){
								//int old_hash = Integer.valueOf(hashStr).intValue(); 

								//					view.put(IDENTIFIER_TAG, uuid);
								//					map.put(view_id,  uuid);
							}
							if(view.containsKey(NODES_TAG)){
								ArrayList<Document> nods = (ArrayList<Document>) view.get(NODES_TAG);
								for(int jj=0;jj<nods.size();jj++){
									Document ob = nods.get(jj);
									cnt = changeIDsInNode(ob, map,view_uuid,cnt, ii);
									//							if( ob.containsKey(ELEMENTREF_TAG)){
									//								String ref = ob.getString(ELEMENTREF_TAG);
									//								uuid = map.get(ref);
									//								ob.put(ELEMENTREF_TAG, uuid);
									//							}
									//							id = ob.getString(IDENTIFIER_TAG);
									//							if(id.equals("id-1993f24b"))
									//								LOGGER.info("found it");
									//							uuid = "id-"+UUID.randomUUID().toString();
									//							ob.put(IDENTIFIER_TAG, uuid);
									//							map.put(id,  uuid);
								}
							}
							if(view.containsKey(CONNECTION_TAG)){
								ArrayList<Document> cons = (ArrayList<Document>) view.get(CONNECTION_TAG);
								for(int jj=0;jj<cons.size();jj++){
									Document ob = cons.get(jj);
									if(ob.containsKey(RELATIONSHIPREF_TAG)){
										String ref = ob.getString(RELATIONSHIPREF_TAG);
										uuid = map.get(ref);
										ob.put(RELATIONSHIPREF_TAG, uuid);
									}
									String src = ob.getString(SOURCE_TAG);
									uuid = map.get(src);
									ob.put(SOURCE_TAG, uuid);
									String trg = ob.getString(TARGET_TAG);
									uuid = map.get(trg);
									ob.put(TARGET_TAG, uuid);
									id = ob.getString(IDENTIFIER_TAG);
									uuid = view_uuid+"-r-"+jj;
									ob.put(IDENTIFIER_TAG, uuid);
									map.put(id,  uuid);
									//					LOGGER.info(ob.toString());
								}
							}
							view.put(IDENTIFIER_TAG, view_uuid);
							map.put(view_id,  view_uuid);
							refs.remove(view_uuid);
							ref_views.add(uuid);
							if(props!=null){
								int hash = getViewHash(view);
								// it is an update
								if(old_hash==hash){
									insert = false; // no update or insert needed
								}
								//	add properties after calculating the hash				}
								view.put(PROPERTIES_TAG, props);
							}

							if(insert){
								overall_insert = true;
								if(update){ 
									overall_update = true;
									factory.retireViewDocument(this, project, branch, user, view_uuid, time);
								}
								factory.insertViewDocument(this, taskId, project, branch, user,  view_uuid, view, time, orgMap.get(view_id));
							}
							//uuid = view.getString(IDENTIFIER_TAG);
							//					Document doc = factory.insertViewDocument(this, project, branch,  view_id, view, time, orgMap.get(view_id));
							//map.put(id, uuid);
						}
						// delete views by deleting the remaining items in refs
						LOGGER.info("#Views to be deleted: "+refs.size());
						for(String ref : refs){
							factory.retireViewDocument(this,project, branch, user, ref, time);
						}

					}
					//
					// handle Management information
					boolean insert = true; // false means no action required
					boolean update = false; 
					int model_hash_new = obj.hashCode();
					String ver = "id-"+UUID.randomUUID().toString();
					if(model_hash_old != 0){
						// there is an entry in the database
						update = true;
						obj.replace(VERSION_TAG, ver);
					} else {
						// there is no entry in the database for this model yet.
						// to make sure that the id is according to our expectation we overwrite the ID
						String old_id = (String) obj.remove(Archimate3Parser.IDENTIFIER_TAG);
						// this is necssary since the JAXB is not compliant with specification; it expects the attributes to be at the beginning of the document
						Set<Entry<String, Object>> entryset = obj.entrySet();
						Document obj2 = new Document();
						obj2.put(Archimate3Parser.IDENTIFIER_TAG, "id-"+UUID.randomUUID().toString());
						obj2.put(Archimate3Parser.VERSION_TAG, ver);
						for(Entry<String,Object> e : entryset){
							if(!e.getKey().equals(Archimate3Parser.IDENTIFIER_TAG) &&
									!e.getKey().equals(Archimate3Parser.VERSION_TAG)){
								obj2.append(e.getKey(), e.getValue());
							}
						}
						obj = obj2;
					}
					if (model_hash_new == model_hash_old) {
						insert = false; // no update or insert needed
					}
					if(overall_insert || insert){
						if(overall_update || update){
							factory.retireManagementDocument(this, project, branch, user, model_id, time);
						}
						factory.insertManagementDocument(this, project, branch, user, obj, time, ref_elements, ref_relations, ref_views);
					}
				}
				
			} // end of if(lock_successull
		}
		factory.releaseBranch(this, project, branch, user);
		LOGGER.severe(retMsg);
		return ret;
	}


	private int changeIDsInNode(Document ob, HashMap<String, String> map, String view_id, int cnt, int view_cnt) {
		String uuid = "";
		if( ob.containsKey(ELEMENTREF_TAG)){
			String ref = ob.getString(ELEMENTREF_TAG);
			uuid = map.get(ref);
			ob.put(ELEMENTREF_TAG, uuid);
		} else {
			uuid = view_id+"-"+view_cnt+"-n-"+(cnt++);	
		}
		uuid = uuid+"-"+view_cnt+"-0";
		String id = ob.getString(IDENTIFIER_TAG);
		ob.put(IDENTIFIER_TAG, uuid);
		map.put(id,  uuid);
		if(ob.containsKey(NODES_TAG)){
			ArrayList<Document> nods = (ArrayList<Document>) ob.get(NODES_TAG);
			for(int jj=0;jj<nods.size();jj++){
				Document ob2 = nods.get(jj);
				cnt = changeIDsInNode(ob2, map,uuid,cnt, view_cnt);
			}
		}
		return cnt;
	}

	private void createOrganizationLookup(String project, String branch, String user, ArrayList<Document> orgs, HashMap<String, Vector<KeyValuePair>> orgMap,
			long time, Vector<KeyValuePair> level, Organization org, Set<String> refs, boolean overall_insert, boolean overall_update) {
		//JSONArray l = orgs.getJSONArray(ITEM_TAG);
		LOGGER.info("number of items on level ("+level.toString()+"): "+orgs.size());
		String value = null;
		for(int ii=0; ii<orgs.size();ii++){
			Document item = (Document) orgs.get(ii);
			//			if(ite instanceof JSONArray){
			//				JSONArray item;
			//				item = (JSONArray) ite;
			if(item.containsKey(LABEL_TAG)){
				ArrayList<Document> labelArr = (ArrayList<Document>) item.get(LABEL_TAG);
				Document label = labelArr.get(0);
				value = label.getString(VALUE_TAG);
				Vector<KeyValuePair> level_call = (Vector<KeyValuePair>) level.clone();
				KeyValuePair kv = new KeyValuePair(value,ii);
				level_call.add(kv);
				//factory.retireOrganizationDocument(this, project, branch, ref, time);
				//				if(level.size()>0){
				//					KeyValuePair kvCall = level.get(level.size()-1);
				if(org.contains(value)){
					//LOGGER.info(org.getChildIDByName(value));
					refs.remove(org.getChildIDByName(value));
					// insert false
					if(org.getChildPositionByName(value)!=ii){
						// update required
						overall_update = true;
						factory.retireManagementDocument(this, project, branch, user, org.getChildIDByName(value), time);
						factory.insertOrganizationDocument(this, project, branch, user, level_call, labelArr,  time);
					}
					//					} 
				}else {
					overall_insert=true;
					factory.insertOrganizationDocument(this, project, branch, user, level_call, labelArr,  time);
				}
			}
			if(item.containsKey(ITEM_TAG)){
				ArrayList<Document> item2 =  (ArrayList<Document>) item.get(ITEM_TAG);
				Vector<KeyValuePair> level_call = (Vector<KeyValuePair>) level.clone();
				if(value!=null && !value.isEmpty()){
					KeyValuePair kv = new KeyValuePair(value,ii);
					level_call.add(kv);
				}
				Organization org_call = org.getChildByName(value);
				if(org_call==null) org_call=org;
				createOrganizationLookup(project, branch, user, item2, orgMap, time, level_call, org_call, refs, overall_insert, overall_update);
			}
			//			} else if(ite instanceof JSONObject){
			//				JSONObject item = (JSONObject) ite;
			if(item.containsKey(IDENTIFIERREF_TAG)){
				String ref = item.getString(IDENTIFIERREF_TAG);
				//	factory.insertOrganizationDocument(this, project, branch, ORGANIZATIONS_TYPE_LABEL, item, level, map.get(ref), time);
				orgMap.put(ref, level);
				//orgs.remove(ii);
			}
			//			}
		}
		//Document doc = factory.insertOrganizationDocument();
	}

	@Override
	public String retrieveJsonString(String taskId, String project, String branch, String user, Date date) {
		String ret1 = "{	\"ar3_model\": {\"@identifier\": \"model1\","+
				"\"ar3_documentation\": [{\"value\": \"Part of the Enterprise Architecture exported to XML\",\"xml_lang\": \"en\"}],"+
				"\"ar3_elements\": \n";
		String ret2 = ",\"ar3_name\": [{\"value\": \"Model with query result\",\"@xml_lang\": \"en\"}],\n"+
				"\"ar3_propertyDefinitions\": \n{\"ar3_propertyDefinition\": \n"+
				"[{\"@identifier\": \""+Archimate3MongoDBConnector.PROPID_IEA_START_DATE+"\",\"@type\": \"number\","+
				"\"ar3_name\" : [ {\"value\" : \""+Archimate3MongoDBConnector.PROPID_IEA_START_DATE+"\"} ]},\n"+
				"{\"@identifier\": \""+Archimate3MongoDBConnector.PROPID_IEA_END_DATE+"\",\"@type\": \"number\","+
				"\"ar3_name\" : [ {\"value\" : \""+Archimate3MongoDBConnector.PROPID_IEA_END_DATE+"\"} ]},\n"+
				"{\"@identifier\": \""+Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER+"\",\"@type\": \"string\","+
				"\"ar3_name\" : [ {\"value\" : \""+Archimate3MongoDBConnector.PROPID_IEA_IDENTIFIER+"\"} ]},\n"+
				"{\"@identifier\": \""+Archimate3MongoDBConnector.PROPID_IEA_HASH+"\",\"@type\": \"string\","+
				"\"ar3_name\" : [ {\"value\" : \""+Archimate3MongoDBConnector.PROPID_IEA_HASH+"\"} ]}\n"+
				"]},\n\"ar3_relationships\": ";
		//		String ret2 = "\n,\"@identifier\": \"model based on query\",\"ar3_name\": [{\"value\": \"Model with query result\",\"@xml_lang\": \"en\"}],"+
		//				"\"ar3_relationships\": [\n";
		String ret3 = "\n,\"@version\": \"1.0\", \n \"ar3_organizations\" :  [ ";
		String ret4 = "\n],\n \"ar3_views\": {\"ar3_diagrams\": \n";
		String ret5 = "\n}}}";

		long time =  date.getTime();
		String ver = "id-"+UUID.randomUUID().toString();
		Document base = factory.retrieveManagementDocument(this, project, branch, user, time);
		Document model = new Document(MODEL_TAG,base);

		// handling propertyDefintions
		Document props = (Document) base.get(PROPERTY_DEFINITIONS_TAG);
		if (props==null){
			props = new Document();
			base.append(PROPERTY_DEFINITIONS_TAG, props);
		}
		ArrayList<Document> prop = (ArrayList<Document>) props.get(PROPERTY_DEFINITION_TAG);
		if(prop==null){
			prop = new ArrayList<Document>();
			props.append(PROPERTY_DEFINITION_TAG, prop);
		}
		HashMap<String, String> propIDs = new HashMap<String,String>();
		propIDs.putAll(Archimate3MongoDBConnector.PROPID_MAP);
		for(int ii=0;ii<prop.size();ii++){
			Document p = prop.get(ii);
			String s = p.getString(IDENTIFIER_TAG);
			propIDs.remove(s);
		}
		// add missing properties
		Iterator<Entry<String, String>> it = propIDs.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String>e = it.next();
			Document p = new Document(IDENTIFIER_TAG, e.getKey()).
					append(PROPERTY_DEFINITION_TYPE_TAG, e.getValue());
			ArrayList<Document> names = new ArrayList<Document>(); 
			names.add(new Document(PROPERTY_VALUE_TAG, e.getKey()));
			p.append(NAME_TAG, names);
			prop.add(p);
		}

		base.replace(VERSION_TAG, ver);
		Collection<String> ref_elements = factory.retrieveAllNodeIDs(this, project,branch);
		Collection<String> ref_relations = factory.retrieveAllRelationshipIDs(this, project,branch);
		Collection<String> ref_views = factory.retrieveAllViewIDs(this, project,branch);
		factory.insertManagementDocument(this, project, branch, user, base, time, ref_elements, ref_relations, ref_views);
		// retrieve the individual pieces
		Organization org = new Organization();
		Document n = factory.retrieveNodeDocument(this, project, branch, user, time, org);
		Document r = factory.retrieveRelationDocument(this, project, branch, user, time, org);
		Document v = factory.retrieveViewDocument(this, project, branch, user, time, org);
		Document o = factory.retrieveOrganizationDocument(this, project, branch, user, time, org);
		base.replace(VERSION_TAG, ver);
		base.replace(ELEMENTS_TAG,n);
		base.replace(RELATIONSHIPS_TAG,r);
		Document diags = (Document) base.get(VIEWS_TAG);
		diags.replace(DIAGRAMS_TAG,v);
		ArrayList<Document> list = new ArrayList<Document>();
		list.add(o);
		base.put(ORGANIZATIONS_TAG,list);
		JsonWriterSettings writerSet = new JsonWriterSettings(true);
		return model.toJson(writerSet);
	}

	@Override
	public int getOrganizationHash(ArrayList<Document> labelArr) {
		return labelArr.hashCode();
	}

	@Override
	protected int getFileHash(JSONObject jsonObject) {
		return jsonObject.hashCode();
	}

	@Override
	public String convertXMLtoJSON(String taskId, String xml) {
		JAXBContext jaxbContext;
		String ret = null;
		try {
			jaxbContext = JAXBContext.newInstance(ModelType.class);

			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
			namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

			//		File file = new File("test3_output.json");
			//			StreamSource source = new StreamSource(new StringBufferInputStream(xml));
			StringReader reader = new StringReader(xml);
			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			//		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
			Object obj = unmarshaller2.unmarshal(reader);
			//JAXBElement<ModelType> result = (JAXBElement<ModelType>) obj;
			ModelType model = (ModelType) obj;

			namespaces = new HashMap<String, String>();
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
			namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			//		jaxbContext =  JAXBContext.newInstance(ModelType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
			marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
			StringWriter out = new StringWriter( );
			marshaller.marshal(model, out);
			ret = out.toString();
			//			out.flush();
			//			out.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TaskStatus ts = factory.getTaskStatus(taskId);
			ts.setMsg(e.getMessage());
			ts.setState(TaskState.FAILURE);
			factory.addTaskStatus(taskId, ts);
		}
		return ret;
	}

	@Override
	public String convertJSONtoXML(String taskId, String json) {
		JAXBContext jaxbContext;
		String ret = null;
		try {
			jaxbContext = JAXBContext.newInstance(ModelType.class);

			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
			namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

			//		File file = new File("test3_output.json");
			//			StreamSource source = new StreamSource(new StringBufferInputStream(xml));
			StringReader reader = new StringReader(json);
			Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			unmarshaller2.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
			//JAXBElement<ModelType> result = (JAXBElement<ModelType>) 
			Object obj = unmarshaller2.unmarshal(reader);
			ModelType model = (ModelType) obj;

			namespaces = new HashMap<String, String>();
			namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
			namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			//		jaxbContext =  JAXBContext.newInstance(ModelType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			//			marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
			//			marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
			//			marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
			//			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			//			marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
			StringWriter out = new StringWriter( );
			marshaller.marshal(model, out);
			ret = out.toString();
			//			out.flush();
			//			out.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TaskStatus ts = factory.getTaskStatus(taskId);
			ts.setMsg(e.getMessage());
			ts.setState(TaskState.FAILURE);
			factory.addTaskStatus(taskId, ts);
		}
		return ret;
	}
}
