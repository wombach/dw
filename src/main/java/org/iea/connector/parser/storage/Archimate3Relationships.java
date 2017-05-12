package org.idw.storage.connector;

public enum Archimate3Relationships {
	INFLUENCE (1,"influence"),
	ACCESS (2,"access"),
	SERVING (3,"serving"),
	REALIZATION (4, "realization"),
	ASSIGNMENT (5, "assignment"), 
	AGGREGATION (6, "aggregation") , 
	COMPOSITION (7, "composition"), 
	SPECIALIZATION (99, "specialization"),
	ASSOCIATION (99, "association"),
	TRIGGERING (99, "triggering"), 
	FLOW (99, "flow");

	private final int weight;
	private final String name;

	Archimate3Relationships(int weight, String name){
		this.weight = weight;
		this.name = name;
	}

	public static int getWeight(String name){
		try{
			Archimate3Relationships n = Archimate3Relationships.valueOf(name.toUpperCase());
			if(n!=null) return n.weight;
		} catch (Exception e){	
		}
		return -1;
	}
}
