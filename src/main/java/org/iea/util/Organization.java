package org.iea.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bson.Document;

public class Organization {

	private String key;
	private ArrayList<Document> label; 
	private TreeMap<String,String> leaves = new TreeMap<String,String>();
	private HashMap<String,Organization> children = new HashMap<String,Organization>();
	private LinkedHashMap<String,Integer> children_pos = new LinkedHashMap<String, Integer>();
	
	public Organization(String key){
		this.key = key;
	}

	public Organization() {
	}

	public ArrayList<Document> getLabel() {
		return label;
	}

	public void setLabel(ArrayList<Document> label) {
		this.label = label;
	}

	public String getKey() {
		return key;
	}

	public TreeMap<String, String> getLeaves() {
		return leaves;
	}
	
	public void addLeaf(String name, String uuid){
		leaves.put(name, uuid);
	}
	
	public void addChild(String name, Organization child){
		children.put(name, child);
	}
	
	public void addChild(String name, Organization child, int pos){
		addChild(name, child);
		children_pos.put(name, pos);
	}
	
	public void setChildPosition(String name, int pos){
		if (children_pos.containsKey(name)){
			children_pos.remove(name);
		} 
		children_pos.put(name,  pos); 
	}
	
	public Organization getChildByName(String name){
		return children.get(name);
	}

	public boolean contains(String na) {
		return children.containsKey(na);
	}
	
	public List<Organization> getChildren(){
		ArrayList<Organization> ret = new ArrayList<Organization>();
		Map<String,Integer> list = MapUtil.sortByValue(children_pos);
		for(String name : list.keySet()){
			ret.add(children.get(name));
		}
		return ret;
	}
	
}
