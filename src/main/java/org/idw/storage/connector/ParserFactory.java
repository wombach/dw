package org.idw.storage.connector;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONObject;

/**
 * Holds all available parsers and allows to dynamically determine the type of a file 
 * @author AN332496
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

	/**
	 * find the right parser and parse a particular file.
	 * Store to mongoDB??? 
	 * @param filename
	 * @return
	 */
	public boolean parseFile(String filename){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.parseFile(filename);
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
	public boolean processXmlString(String str){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.processXmlString(str);
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
	public boolean processJsonString(String str){
		boolean ret = false;
		for(GenericParser gp: parsers.values()){
			ret = gp.processJsonString(str);
			if (ret) break;
		}
		return ret;
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return
	 */
	public void deriveFile(String parserName, String filename, Date date){
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			gp.deriveFile(filename, date);
		}
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
	public String deriveXmlString(String parserName, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.deriveXmlString(date);
		}
		return ret;
	}

	/**
	 * derive the file from mongoDB based on the specified query
	 * @param query
	 * @return String with the retrieved model
	 */
	public String deriveJsonString(String parserName, Date date){
		String ret = null;
		if(parsers.containsKey(parserName)){
			GenericParser gp = parsers.get(parserName);
			ret = gp.deriveJsonString(date);
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

	public Document insertNodeDocument(GenericParser parser, JSONObject n, long time) {
		return storage.insertNodeDocument(parser, n, time);
	}

	public Document insertRelationDocument(GenericParser parser, JSONObject rel, String sourceUUID, String targetUUID, long time) {
		return storage.insertRelationDocument(parser, rel, sourceUUID, targetUUID, time) ;
	}

}
