package org.iea.pool;


import java.util.logging.Logger;

import org.iea.util.DifResult;

public class TaskStatus {
	private final static Logger LOGGER = Logger.getLogger(TaskStatus.class.getName());
	private int noNodes = 0;
	private int noRelations = 0;
	private int noViews = 0;
	private String msg = "";
	private long time = 0;
	private TaskState state = TaskState.CREATED;
	private DifResult difResult = null;  
	
	public DifResult getDifResult() {
		return difResult;
	}

	public void setDifResult(DifResult difResult) {
		this.difResult = difResult;
	}

	public TaskStatus(){};
	
	public TaskStatus(int noNodes, int noRelations, int noViews, String msg){
		this.noNodes = noNodes;
		this.noRelations = noRelations;
		this.noViews = noViews;
		this.msg = msg;
		this.time = System.currentTimeMillis();
	}

	public int getNoNodes() {
		return noNodes;
	}

	public void incNoNodes() {
		noNodes++;
		this.time = System.currentTimeMillis();
	}

	public void setNoNodes(int noNodes) {
		this.noNodes = noNodes;
		this.time = System.currentTimeMillis();
	}

	public int getNoRelations() {
		return noRelations;
	}

	public void incNoRelations() {
		noRelations++;
		this.time = System.currentTimeMillis();
	}

	public void setNoRelations(int noRelations) {
		this.noRelations = noRelations;
		this.time = System.currentTimeMillis();
	}

	public int getNoViews() {
		return noViews;
	}

	public void incNoViews() {
		noViews++;
		this.time = System.currentTimeMillis();
	}

	public void setNoViews(int noViews) {
		this.noViews = noViews;
		this.time = System.currentTimeMillis();
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
		this.time = System.currentTimeMillis();
	}
	
	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public long getTime() {
		return time;
	}

	public String getJsonObjectString(){
		String str = "{\"noNodes\":"+noNodes+", \"noRelations\":"+noRelations+", \"noViews\":"+
				noViews+", \"message\":\""+msg+"\", \"timestamp\":"+time+
				", \"state\": \""+state+"\", \"difResult\": ";
		if(difResult!=null) str +=difResult.getJsonObjectString();
		else str += "null ";
		str+="}";
		return str;
	}
}
