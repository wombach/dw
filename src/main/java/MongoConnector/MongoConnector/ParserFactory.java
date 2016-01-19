package MongoConnector.MongoConnector;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Holds all available parsers and allows to dynamically determine the type of a file 
 * @author AN332496
 *
 */
public class ParserFactory {

	private final static Logger LOGGER = Logger.getLogger(ParserFactory.class.getName()); 

	private HashMap<String,GenericParser> parsers = new HashMap<String,GenericParser>();
	
	public ParserFactory(){
	}
	
	public void registerParser(String parserName, GenericParser gp){
		parsers.put(parserName, gp);
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
	
}
