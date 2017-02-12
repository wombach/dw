package org.idw.storage.connector;

public enum Archimate3Relationships {
	ASSOCIATION (1, "association"),
	COMPOSITION (2, "composition"), 
	ASSIGNMENT (2, "assignment"), 
	TRIGGERING (3, "triggering"), 
	FLOW (3, "flow"), 
	ACCESS (3,"access"),
	INFLUENCE (3,"influence"),
	SERVING (3,"serving"),
	AGGREGATION (3, "aggregation") , 
	REALIZATION (3, "realization"),
	SPECIALIZATION (4, "specialization");
	
	private final int weight;
	private final String name;
	
	Archimate3Relationships(int weight, String name){
		this.weight = weight;
		this.name = name;
	}
	
	public static int getWeight(String name){
		Archimate3Relationships n = Archimate3Relationships.valueOf(name.toUpperCase());
		if(n==null) return -1;
		return n.weight;
	}
}
