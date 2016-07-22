package MongoConnector.MongoConnector;

import java.util.Date;
import java.util.logging.Logger;

public class BPMNParser extends GenericParser{
	private final static Logger LOGGER = Logger.getLogger(BPMNParser.class.getName());
	public final static String URI = "http://org.omg.spec.bpmn._20100524.model";

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
