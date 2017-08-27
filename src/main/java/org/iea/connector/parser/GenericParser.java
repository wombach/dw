package org.iea.connector.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.pool.TaskState;
import org.iea.pool.TaskStatus;
import org.iea.util.DifRecord;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class GenericParser {

	private final static Logger LOGGER = Logger.getLogger(GenericParser.class.getName()); 
	public final static int PRETTY_PRINT_INDENT_FACTOR = 4;
	public final static String DOC_NAME = "name";
	public final static String DOC_NAME_NODE = "node";
	public final static String DOC_TYPE = "type";
	public final static String DOC_START_DATE = "start_date";
	public final static String DOC_END_DATE = "end_date";
	public final static String DOC_RAW = "raw";
	public final static String DOC_RAW_ELEMENT = "element";
	public final static String DOC_ID = "id";
	public final static String DOC_COMPARISON_STRING = "comparison_string";
	public static final String DOC_HASH = "hash";

	protected String type = null;
	protected String CONTEXT = null;
	protected Class MODEL_CLASS = null;
//	protected Map<String, String> namespaces;
	protected ParserFactory factory;
//	protected String taskId;

	public GenericParser(){
//		namespaces = new HashMap<String, String>();
//		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
//		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
//		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
	}

	//	public abstract boolean parseFile(String project, String branch, String filename2);

	//	public abstract Object parseXmlString(String str);
	//	public abstract Object parseJsonString(String str, String branch, String str2);
	//	public abstract boolean storeObject(Object elm);
	//	
	//	public boolean processXmlString(String str, String branch, String user, String str2){
	//		boolean ret = false;
	//		Object elm = parseXmlString(str);
	//		if(elm!=null){
	//			ret = storeObject(elm);
	//		}
	//		return ret;	
	//	};

	public Vector<DifRecord> processXmlString(String taskId, String project, String branch, String user, String str, boolean overwrite, Set<String> keepList){
		Vector<DifRecord> ret = null;
		String json_str= convertXMLtoJSON(taskId, str);
		ret = processJsonString(taskId, project, branch, user, json_str, overwrite, keepList);
		return ret;
	}
	public abstract  Vector<DifRecord> processJsonString(String taskId, String project, String branch, String user, String json, boolean overwrite, Set<String> keepList);
	public abstract int getNodeHash(Document jsonObject);

	public abstract int getRelationHash(Document jsonObject);

	abstract protected int getFileHash(JSONObject jsonObject);

	public String prettyPrintJSON(JSONObject xmlJSONObj){
		String ret = "";
		try {
			ret = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
		} catch (JSONException je) {
			LOGGER.severe(je.toString());
		}
		return ret;
	}

	public abstract String convertXMLtoJSON(String taskId, String xml);

	public abstract String convertJSONtoXML(String taskId, String json);

	public String getType(){
		return type;
	}

	public void setFactory(ParserFactory parserFactory) {
		this.factory = parserFactory; 
	}

	public abstract String retrieveJsonString(String taskId, String project, String branch, String user, Date date);

	public abstract int getOrganizationHash(ArrayList<Document> labelArr);

	public abstract int getViewHash(Document jsonObject);

	public abstract int getManagmentHash(Document jsonObject);

	//public abstract Vector<DifRecord> enrichDifList(String taskId, HashMap<String, Document> difList);

//	protected TaskStatus getTaskStatus(String taskId){
//		return factory.getTaskStatus(taskId);
//	}
//	
//	public void setNoNodes(String taskId, int noNodes) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.setNoNodes(noNodes);
//	}
//	public void incNoNodes(String taskId) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.incNoNodes();
//	}
//	
//	public void incNoRelations(String taskId) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.incNoRelations();
//	}
//	
//	public void incNoViews(String taskId) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.incNoViews();
//	}
//	public void setNoRelations(String taskId, int noRelations) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.setNoRelations(noRelations);
//	}
//	
//	public void setNoViews(String taskId, int noViews) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.setNoViews(noViews);
//	}
//	
//	public void setMsg(String taskId, String msg) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.setMsg(msg);
//	}
//	
//	public void setState(String taskId, TaskState state) {
//		TaskStatus ts = getTaskStatus(taskId);
//		if(ts!=null) ts.setState(state);
//	}
}