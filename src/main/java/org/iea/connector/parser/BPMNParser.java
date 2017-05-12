package org.iea.connector.parser;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONObject;

public class BPMNParser extends GenericParser{
	private final static Logger LOGGER = Logger.getLogger(BPMNParser.class.getName());
	public final static String URI = "http://org.omg.spec.bpmn._20100524.model";

	public final long TIME = 0;
	public BPMNParser(){
		type = "bpmn";
	}
	
	@Override
	public boolean parseFile(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deriveFile(String filename, Date date) {
		// TODO Auto-generated method stub
	}


	@Override
	protected String getNodeComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNodeHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getRelationComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getRelationHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getFileComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getFileHash(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean processXmlString(String str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processJsonString(String str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String deriveXmlString(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deriveJsonString(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseXmlString(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseJsonString(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeObject(Object elm) {
		// TODO Auto-generated method stub
		return false;
	} 


}
