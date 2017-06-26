package org.iea.connector.parser;

import java.util.ArrayList;
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
	public boolean parseFile(String project, String branch, String filename){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.parseFile(project, branch, filename);
			if (ret) break;
		}
		return ret;
	}

	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param filename
	 * @return
	 */
	public boolean processXmlString(String project, String branch, String str){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.processXmlString(project, branch, str);
			if (ret) break;
		}
		return ret;
	}

	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param json 
	 * @param string 
	 * @param filename
	 * @return
	 */
	public boolean processJsonString(String project, String branch, String json){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.processJsonString(project, branch, json);
			if (ret) break;
		}
		return ret;
	}

	public Object parseJsonString(String project, String branch, String str){
		Object ret = null;
		for(GenericParser gp: parsers.values()){
			ret = gp.parseJsonString(project, branch, str);
			if (ret!=null) break;
		}
		return ret;
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return
	 */
	public void deriveFile(String parserName, String project, String branch, String filename, Date date){
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			gp.deriveFile(project, branch, filename, date);
		}
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
	public String deriveXmlString(String parserName, String project, String branch, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.deriveXmlString(project, branch, date);
		}
		return ret;
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
	public String deriveJsonString(String parserName, String project, String branch, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.deriveJsonString(project, branch, date);
		}
		return ret;
	}

	public String retrieveJsonString(String parserName, String project, String branch, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.retrieveJsonString(project, branch, date);
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

	public Document retrieveNodeDocument(GenericParser parser, String project, String branch, long time, Organization org) {
		return storage.retrieveNodeDocument(parser, project, branch,time, org);
	}
	public Document retrieveRelationDocument(GenericParser parser, String project, String branch, long time, Organization org) {
		return storage.retrieveRelationDocument(parser, project, branch,time, org);
	}
	public Document retrieveViewDocument(GenericParser parser, String project, String branch, long time, Organization org) {
		return storage.retrieveViewDocument(parser, project, branch,time, org);
	}

	public Document insertNodeDocument(GenericParser parser, String project, String branch, Document n, long time, Vector<KeyValuePair> org) {
		return storage.insertNodeDocument(parser, project, branch, n, time, org);
	}

	public Document insertRelationDocument(GenericParser parser,String project, String branch, Document rel, String sourceUUID, Document source, String targetUUID, Document target, long time, Vector<KeyValuePair> org) {
		return storage.insertRelationDocument(parser, project, branch, rel, sourceUUID, source, targetUUID, target, time, org) ;
	}

	public Document insertViewDocument(GenericParser parser, String project, String branch,String uuid, Document view, long time, Vector<KeyValuePair> org) {
		return storage.insertViewDocument(parser, project, branch, uuid, view, time, org);
	}

	public String writeJSONtoXML(String parserName, String t) {
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.writeJSONtoXML(t);
		}
		return ret;
	}

	public Document insertOrganizationDocument(GenericParser parser, String project, String branch,
			Vector<KeyValuePair> level, ArrayList<Document> labelArr, long time) {
		return storage.insertOrganizationDocument(parser, project, branch, level, labelArr,  time) ;
	}

	public Document retrieveOrganizationDocument(GenericParser parser, String project, String branch, long time, Organization org) {
		return storage.retrieveOrganizationDocument(parser, project, branch, time, org);
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

	public void retireNodeDocument(GenericParser parser, String project, String branch, String ref, long time) {
		storage.retireNodeDocument(parser, project, branch, ref, time) ;
	}

	public void retireRelationshipDocument(GenericParser parser, String project, String branch, String ref, long time) {
		storage.retireRelationshipDocument(parser, project, branch, ref, time) ;
	}

	public void retireViewDocument(GenericParser parser, String project, String branch, String ref, long time) {
		storage.retireViewDocument(parser, project, branch, ref, time) ;
	}

//	public Document insertOrganizationDocument(Archimate3Parser archimate3Parser, String project, String branch, String organizationsTypeLabel,
//			JSONObject item, Vector<String> level, String value, long time) {
//		return storage.insertOrganizationDocument(archimate3Parser, project, branch, organizationsTypeLabel, item, level, value, time);
//	}

}
