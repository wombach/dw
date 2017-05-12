package org.idw.storage.connector;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.Document;
import org.json.JSONObject;

public abstract class GenericParserStorageConnector {
	private final static Logger LOGGER = Logger.getLogger(GenericParserStorageConnector.class.getName()); 

	protected GenericParser parser;
		
	protected GenericParser getParser(){
		return parser;
	}
	
	protected void setParser(GenericParser parser){
		this.parser = parser;
	}
	
}
