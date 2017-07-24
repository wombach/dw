package org.iea.pool;


public enum TaskState {
	CREATED ("created"), 
	WAITING ("waiting"), 
	RUNNING ("running"), 
	COMPLETED ("completed"), 
	FAILURE ("failure");

    private final String name;       

    private TaskState(String s) {
        name = s;
    }

    public boolean equalsState(String otherState) {
        // (otherName == null) check is not needed because name.equals(null) returns false 
        return name.equals(otherState);
    }

    public String toString() {
       return this.name;
    }
}
