package org.iea.connector.parser.storage;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.Document;
import org.iea.connector.parser.GenericParser;
import org.json.JSONObject;

public abstract class GenericParserStorageConnector {
	private final static Logger LOGGER = Logger.getLogger(GenericParserStorageConnector.class.getName()); 

	protected GenericParser parser;
		
	public GenericParser getParser(){
		return parser;
	}
	
	public void setParser(GenericParser parser){
		this.parser = parser;
	}
	
}
