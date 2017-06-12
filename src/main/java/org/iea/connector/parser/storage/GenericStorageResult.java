package org.iea.connector.parser.storage;

import org.bson.Document;

import com.mongodb.BasicDBObject;

public class GenericStorageResult {
	private Document doc = null;
	private int status = -1;
	
	public GenericStorageResult(){}
	
	public GenericStorageResult(Document doc, int status){
		this.doc=doc;
		this.status=status;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setStatusInserted(){
		status = 1;
	}
	
	public void setStatusUpdated(){
		status = 2;
	}
	
	public void setStatusUnchanged(){
		status = 0;
	}
	
	public boolean isStatusInserted(){
		return status==1;
	}
	
	public boolean isStatusUpdated(){
		return status==2;
	}
	
	public boolean isStatusUnchanged(){
		return status==0;
	}
	
}
