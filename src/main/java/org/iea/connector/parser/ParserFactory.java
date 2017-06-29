package org.iea.connector.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.storage.StorageFactory;
import org.iea.connector.storage.StorageRegistrationException;
import org.iea.util.KeyValuePair;
import org.iea.util.Organization;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

/**
 * Holds all available parsers and allows to dynamically determine the type of a file 
 * @author wombach
 *
 */
public class ParserFactory {

	private final static Logger LOGGER = Logger.getLogger(ParserFactory.class.getName()); 

	private HashMap<String,GenericParser> parsers = new HashMap<String,GenericParser>();
	private StorageFactory storage = new StorageFactory();

	public ParserFactory(){
	}

	public void registerParser(String parserName, GenericParser gp){
		parsers.put(parserName, gp);
		gp.setFactory(this);
	}

	public void dropDB(){
		storage.dropDB();
	}
	
	public void dropProject(String project){
		storage.dropProject(project);
	}
	
	public void dropBranch(String project, String branch){
		
	}
	
	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param filename
	 * @return
	 */
//	public boolean parseFile(String project, String branch, String user, String filename){
//		boolean ret = false;
//		for(GenericParser gp: parsers.values()){
//			ret = gp.parseFile(project, branch,filename);
//			if (ret) break;
//		}
//		return ret;
//	}

	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param filename
	 * @return
	 */
//	public boolean processXmlString(String project, String branch, String user, String str){
//		boolean ret = false;
//		for(GenericParser gp: parsers.values()){
//			ret = gp.processXmlString(project, branch, str);
//			if (ret) break;
//		}
//		return ret;
//	}

	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param json 
	 * @param string 
	 * @param filename
	 * @return
	 */
	public boolean processJsonString(String project, String branch, String user, String json){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.processJsonString(project, branch, user, json);
			if (ret) break;
		}
		return ret;
	}

//	public Object parseJsonString(String project, String branch, String user, String str){
//		Object ret = null;
//		for(GenericParser gp: parsers.values()){
//			ret = gp.parseJsonString(project, branch, str);
//			if (ret!=null) break;
//		}
//		return ret;
//	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return
	 */
//	public void deriveFile(String parserName, String project, String branch, String user, String filename, Date date){
//		if(parsers.containsKey(parserName)){
//			GenericParser gp = parsers.get(parserName);
//			gp.deriveFile(project, branch, user, filename, date);
//		}
//	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
//	public String deriveXmlString(String parserName, String project, String branch, String user, Date date){
//		String ret = null;
//		if(parsers.containsKey(parserName)){
//			GenericParser gp = parsers.get(parserName);
//			ret = gp.deriveXmlString(project, branch, user, date);
//		}
//		return ret;
//	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
//	public String deriveJsonString(String parserName, String project, String branch, String user, Date date){
//		String ret = null;
//		if(parsers.containsKey(parserName)){
//			GenericParser gp = parsers.get(parserName);
//			ret = gp.deriveJsonString(project, branch, user, date);
//		}
//		return ret;
//	}

	public String retrieveJsonString(String parserName, String project, String branch, String user, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.retrieveJsonString(project, branch, user, date);
		}
		return ret;
	}

	public boolean registerStorage(String storageName, GenericParserStorageConnector gs, boolean managingIDs) {
		GenericParser p = parsers.get(storageName);
		boolean flag = false;
		if(p!=null){
			try {
				storage.registerStorage(p, gs, managingIDs);
				gs.setParser(p);
				flag = true;
			} catch (StorageRegistrationException e) {
				LOGGER.severe("ParserStorageConnector with multiple managingIDs attempted to register!" );
			}
		}
		return flag;
	}

	public Document retrieveNodeDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		return storage.retrieveNodeDocument(parser, project, branch, user,time, org);
	}
	public Document retrieveRelationDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		return storage.retrieveRelationDocument(parser, project, branch, user,time, org);
	}
	public Document retrieveViewDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		return storage.retrieveViewDocument(parser, project, branch, user,time, org);
	}

	public Document insertNodeDocument(GenericParser parser, String project, String branch, String user, Document n, long time, Vector<KeyValuePair> org) {
		return storage.insertNodeDocument(parser, project, branch, user, n, time, org);
	}

	public Document insertManagementDocument(GenericParser parser, String project, String branch, String user, Document n, long time, 
			Collection<String> ref_elements, Collection<String> ref_relations, Collection<String> ref_views) {
		return storage.insertManagementDocument(parser, project, branch, user, n, time, ref_elements, ref_relations, ref_views);
	}

	public Document insertRelationDocument(GenericParser parser,String project, String branch, String user, Document rel, String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org) {
		return storage.insertRelationDocument(parser, project, branch, user, rel, sourceUUID, source, targetUUID, target, time, org) ;
	}

	public Document insertViewDocument(GenericParser parser, String project, String branch, String user,String uuid, Document view, long time, Vector<KeyValuePair> org) {
		return storage.insertViewDocument(parser, project, branch, user, uuid, view, time, org);
	}

//	public String writeJSONtoXML(String parserName, String t) {
//		String ret = null;
//		if(parsers.containsKey(parserName)){
//			GenericParser gp = parsers.get(parserName);
//			ret = gp.writeJSONtoXML(t);
//		}
//		return ret;
//	}

	public Document insertOrganizationDocument(GenericParser parser, String project, String branch, String user,
			Vector<KeyValuePair> level, ArrayList<Document> labelArr, long time) {
		return storage.insertOrganizationDocument(parser, project, branch, user, level, labelArr,  time) ;
	}

	public Document retrieveOrganizationDocument(GenericParser parser, String project, String branch, String user, long time, Organization org) {
		return storage.retrieveOrganizationDocument(parser, project, branch, user, time, org);
	}

	public Document retrieveManagementDocument(GenericParser parser, String project, String branch, String user, long time) {
		return storage.retrieveManagementDocument(parser, project, branch, user, time);
	}

	public Set<String> retrieveAllNodeIDs(GenericParser parser, String project, String branch) {
		return storage.retrieveAllNodeIDs(parser, project, branch) ;
	}

	public Set<String> retrieveAllRelationshipIDs(GenericParser parser, String project, String branch) {
		return storage.retrieveAllRelationshipIDs(parser, project, branch) ;
	}

	public Set<String> retrieveAllViewIDs(GenericParser parser, String project, String branch) {
		return storage.retrieveAllViewIDs(parser, project, branch) ;
	}

	public Set<String> retrieveAllOrganizationIDs(GenericParser parser, String project, String branch) {
		return storage.retrieveAllOrganizationIDs(parser, project, branch) ;
	}

	public Set<String> retrieveFileNodeIDs(GenericParser parser, String project, String branch, String fileID, String version) {
		return storage.retrieveFileNodeIDs(parser, project, branch, fileID, version) ;
	}

	public Set<String> retrieveFileRelationshipIDs(GenericParser parser, String project, String branch, String fileID, String version) {
		return storage.retrieveFileRelationshipIDs(parser, project, branch, fileID, version) ;
	}

	public Set<String> retrieveFileViewIDs(GenericParser parser, String project, String branch, String fileID, String version) {
		return storage.retrieveFileViewIDs(parser, project, branch, fileID, version) ;
	}

//	public Set<String> retrieveFileOrganizationIDs(GenericParser parser, String project, String branch, String fileID) {
//		return storage.retrieveFileOrganizationIDs(parser, project, branch, fileID) ;
//	}

	public void retireNodeDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		storage.retireNodeDocument(parser, project, branch, user, ref, time) ;
	}

	public void retireOrganizationDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		storage.retireOrganizationDocument(parser, project, branch, user, ref, time) ;
	}

	public void retireManagementDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		storage.retireManagementDocument(parser, project, branch, user, ref, time) ;
	}

	public void retireRelationshipDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		storage.retireRelationshipDocument(parser, project, branch, user, ref, time) ;
	}

	public void retireViewDocument(GenericParser parser, String project, String branch, String user, String ref, long time) {
		storage.retireViewDocument(parser, project, branch, user, ref, time) ;
	}

	public void retrieveOrganization(GenericParser parser, String project, String branch, String user, long time,	Organization org) {
		storage.retrieveOrganization(parser, project, branch, user, time, org) ;		
	}

	public boolean lockBranch(GenericParser parser, String project, String branch, String user, String model_id, long time) {
		return storage.lockBranch(parser, project, branch, user, model_id, time) ;
	}

	public void releaseBranch(GenericParser parser, String project, String branch, String user) {
		storage.releaseBranch(parser, project, branch, user) ;
	}

	public int retrieveModelHash(GenericParser parser, String project, String branch, String user, String model_id, long time) {
		return storage.retrieveModelHash(parser, project, branch, user, model_id, time) ;
	}

	public boolean checkModelCommit(GenericParser parser, String project, String branch, String model_id,
			String version) {
		return storage.checkModelCommit(parser, project, branch, model_id, version) ;
	}



//	public Document insertOrganizationDocument(Archimate3Parser archimate3Parser, String project, String branch, String user, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time) {
//		return storage.insertOrganizationDocument(archimate3Parser, project, branch, user, organizationsTypeLabel, item, level, value, time);
//	}

}
