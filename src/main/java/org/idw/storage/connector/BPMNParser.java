package org.idw.storage.connector;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONObject;

public class BPMNParser extends GenericParser{
	private final static Logger LOGGER = Logger.getLogger(BPMNParser.class.getName());
	public final static String URI = "http://org.omg.spec.bpmn._20100524.model";

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
	public boolean parseString(String str) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String deriveString(Date date) {
		// TODO Auto-generated method stub
		return null;
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


}
