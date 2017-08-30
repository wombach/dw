package org.iea.util;

import java.util.Set;
import java.util.Vector;

public class DifResult {

	private Vector<DifRecord> list;
	private long ts;
	private ProcessingState state;

	public DifResult(long ts){
		this.state = ProcessingState.UNDEFINED;
		this.ts = ts;
	}

	//	public void setCommit(){
	//		this.state = ProcessingState.COMMIT;
	//	}
	//	
	public void setCommitted(){
		this.state = ProcessingState.COMMITTED;
	}

	public void setLocked(){
		this.state = ProcessingState.LOCKED;
	}

	public void setConflict(Vector<DifRecord> list){
		this.state = ProcessingState.CONFLICT;
		this.list = list;
	}

	public String getJsonObjectString() {
		String str = "{\"state\":\""+state+"\", \"timestamp\":"+ts+", \"list\":[";
		boolean first = true;
		if(list!=null){
			for(DifRecord dif : list){
				if(!first) str+=", ";
				str += dif.getJsonObjectString();
			}
		}
		return str+"]}";
	}

	public boolean isCommitted(){
		return this.state == ProcessingState.COMMITTED;
	}

	public boolean isConflict(){
		return this.state == ProcessingState.CONFLICT;
	}
}
