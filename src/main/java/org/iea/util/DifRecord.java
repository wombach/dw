package org.iea.util;

import org.bson.Document;

public class DifRecord {
	private Document left;
	private Document right;
	private String type;
	
	public DifRecord(Document left, Document right, String type){
		this.left = left;
		this.right = right;
		this.type = type;
	}
	
	public Document getLeft(){
		return this.left;
	}
	
	public Document getRight(){
		return right;
	}
	
	public String type(){
		return type;
	}
}
