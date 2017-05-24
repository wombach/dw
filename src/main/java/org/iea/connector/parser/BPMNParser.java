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
	public boolean parseFile(String project, String branch,String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deriveFile(String project, String branch,String filename, Date date) {
		// TODO Auto-generated method stub
	}


	@Override
	public String getNodeComparisonString(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNodeHash(JSONObject jsonObject) {
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
	public boolean processXmlString(String project, String branch,String str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processJsonString(String project, String branch,String str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String deriveXmlString(String project, String branch,Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deriveJsonString(String project, String branch,Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseXmlString(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parseJsonString(String project, String branch,String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeObject(Object elm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String retrieveJsonString(String project, String branch, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String writeJSONtoXML(String st) {
		// TODO Auto-generated method stub
		return null;
	} 


}
