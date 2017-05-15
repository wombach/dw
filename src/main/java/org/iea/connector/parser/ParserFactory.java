package org.iea.connector.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.storage.GenericParserStorageConnector;
import org.iea.connector.storage.StorageFactory;
import org.iea.connector.storage.StorageRegistrationException;
import org.json.JSONObject;

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

	public Document retrieveNodeDocument(GenericParser parser, String project, String branch, long time) {
		return storage.retrieveNodeDocument(parser, project, branch,time);
	}
	public Document retrieveRelationDocument(GenericParser parser, String project, String branch, long time) {
		return storage.retrieveRelationDocument(parser, project, branch,time);
	}
	public Document retrieveViewDocument(GenericParser parser, String project, String branch, long time) {
		return storage.retrieveViewDocument(parser, project, branch,time);
	}

	public Document insertNodeDocument(GenericParser parser, String project, String branch, JSONObject n, long time) {
		return storage.insertNodeDocument(parser, project, branch, n, time);
	}

	public Document insertRelationDocument(GenericParser parser,String project, String branch, String uuid, JSONObject rel, String sourceUUID, String targetUUID, long time) {
		return storage.insertRelationDocument(parser, project, branch, uuid, rel, sourceUUID, targetUUID, time) ;
	}

	public Document insertViewDocument(Archimate3Parser archimate3Parser, String project, String branch,String uuid, JSONObject view, long time) {
		return storage.insertViewDocument(archimate3Parser, project, branch, uuid, view, time);
	}

	public String writeJSONtoXML(String parserName, String t) {
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.writeJSONtoXML(t);
		}
		return ret;
	}

}
