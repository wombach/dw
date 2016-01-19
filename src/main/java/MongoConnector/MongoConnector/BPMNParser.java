package MongoConnector.MongoConnector;

import java.util.Date;
import java.util.logging.Logger;

public class BPMNParser extends GenericParser{
	private final static Logger LOGGER = Logger.getLogger(BPMNParser.class.getName());
	
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


}
