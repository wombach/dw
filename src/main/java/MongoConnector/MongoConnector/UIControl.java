package MongoConnector.MongoConnector;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.bson.Document;
import org.xml.sax.SAXException;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import java.util.Date;
import java.util.logging.Logger;

public class UIControl {
	
	private final static Logger LOGGER = Logger.getLogger(UIControl.class.getName()); 
	public final static MongoDBAccess mongo = new MongoDBAccess();
	public ParserFactory pf = new ParserFactory();
	
	private boolean parseFile(String filename){
		return pf.parseFile(filename);
	}

	public void deriveFile(String parserName, String filename, Date date) {
		pf.deriveFile(parserName, filename, date);
	}

	private void registerParser(String parserName, GenericParser gp) {
		pf.registerParser(parserName, gp);
	}

	/**
	 * static UI; later to be replaced by a web based interface
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		UIControl u = new UIControl();
		u.registerParser("archimate", new ArchimateParser());
		u.registerParser("bpmn", new BPMNParser());
		
		// insert an archimate file into mongoDB
		mongo.dropCollections();
//		boolean r = u.parseFile("OTK Sample.xml");
		boolean r = u.parseFile("whr_line_6.xml");
		LOGGER.info("file parsed :"+r);
		mongo.getAllDocuments();
		
		// retrieve an archimate file into mongoDB
		Date date = new Date(System.currentTimeMillis());
		u.deriveFile("archimate", "test_whr.xml", date);
	}

}
