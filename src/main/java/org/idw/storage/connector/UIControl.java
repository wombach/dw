package org.idw.storage.connector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.bson.Document;
import org.xml.sax.SAXException;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import java.util.Date;
import java.util.logging.Logger;

public class UIControl {
	
	private final static Logger LOGGER = Logger.getLogger(UIControl.class.getName());
	private static MongoDBAccess mongo = new MongoDBAccess();
	private static Neo4jAccess graph = new Neo4jAccess();
	public ParserFactory pf = new ParserFactory();
//	private static final String jsonStr = "{\"ns0_model\" : {\"identifier\" : \"test\",\"ns0_elements\" : {\"ns0_element\" : [ "+
//			"{\"identifier\" : \"test2\", \"ns1_type\" : \"ns0_BusinessProcess\", \"ns0_properties\" : { \"ns0_property\" : [ {"+
//            "\"propertyDefinitionRef\" : \"hallo13\",\"ns0_value\" : [ {\"xml_lang\" : \"en\",\"value\" : \"test3\"} ] } ]}} ]},"+
//            "\"ns0_propertyDefinitions\" : {\"ns0_propertyDefinition\" : [ {\"identifier\" : \"hallo12\",\"ns1_type\" : \"number\"}, {"+
//            "\"identifier\" : \"hallo13\",\"ns1_type\" : \"number\"} ]}}}; ";
//	private static final String jsonStr = "{\"model\" : {\"identifier\" : \"test\",\"elements\" : {\"element\" : [ "+
//			"{\"identifier\" : \"test2\", \"type\" : \"BusinessProcess\", \"properties\" : { \"property\" : [ {"+
//            "\"propertyDefinitionRef\" : \"hallo13\",\"value\" : [ {\"xml_lang\" : \"en\",\"value\" : \"test3\"} ] } ]}} ]},"+
//            "\"propertyDefinitions\" : {\"propertyDefinition\" : [ {\"identifier\" : \"hallo12\",\"type\" : \"number\"}, {"+
//            "\"identifier\" : \"hallo13\",\"type\" : \"number\"} ]}}}; ";
	
	private static final String jsonStr = "{	   \"model\" : { 		      \"identifier\" : \"model-1\", 		      \"version\" : \"1.0\", 		      \"name\" : [ { 		         \"lang\" : \"en\", 		         \"value\" : \"Test Model\" 		      } ], 		      \"documentation\" : [ { 		         \"lang\" : \"en\", 		         \"value\" : \"Part of the Enterprise Architecture exported to XML\" 		      } ], 		      \"elements\" : { 		         \"element\" : [ { 		            \"identifier\" : \"element-97\", 		            \"type\" : \"BusinessProcess\", 		            \"name\" : [ { 		               \"lang\" : \"en\", 		               \"value\" : \"Business process (2)\" 		            } ] 		         }, { 		            \"identifier\" : \"element-96\", 		            \"type\" : \"BusinessProcess\", 		            \"name\" : [ { 		               \"lang\" : \"en\", 		               \"value\" : \"Business process\" 		            } ] 		         } ] 		      }, 		      \"relationships\" : { 		         \"relationship\" : [ { 		            \"identifier\" : \"relation-119\", 		            \"source\" : \"element-96\", 		            \"target\" : \"element-97\", 		            \"type\" : \"Flow\", 		            \"name\" : [ { 		               \"lang\" : \"en\", 		               \"value\" : \"\" 		            } ] 		         },{ 		            \"identifier\" : \"relation-1191\", 		            \"source\" : \"element-96\", 		            \"target\" : \"relation-119\", 		            \"type\" : \"Flow\", 		            \"name\" : [ { 		               \"lang\" : \"en\", 		               \"value\" : \"\" 		            } ] 		         }, { 		            \"identifier\" : \"relation-99\", 		            \"source\" : \"element-96\", 		            \"target\" : \"element-97\", 		            \"type\" : \"Triggering\", 		            \"name\" : [ { 		               \"lang\" : \"en\", 		               \"value\" : \"\" 		            } ] 		         } ] 		      } 		   } 		} ";
	
	private String readFile(String filename){
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(filename)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	
	private boolean parseFile(String filename){
		return pf.parseFile(filename);
	}
	private boolean parseJsonString(String json){
		return pf.processJsonString(json);
	}

	public static MongoDBAccess getMongo(){
    	if(mongo==null){
    		mongo = new MongoDBAccess();
    	}
    	return mongo;
    }
	
	public static Neo4jAccess getGraph() {
    	if(graph==null){
    		graph = new Neo4jAccess();
    	}
    	return graph;
	}

    
	public void deriveFile(String parserName, String filename, Date date) {
		pf.deriveFile(parserName, filename, date);
	}

	public String deriveXmlString(String parserName, Date date) {
		return pf.deriveXmlString(parserName, date);
	}

	public String deriveJsonString(String parserName, Date date) {
		return pf.deriveJsonString(parserName, date);
	}

	private void registerParser(String parserName, GenericParser gp) {
		pf.registerParser(parserName, gp);
	}
	
	public boolean  registerStorage(String storageName, GenericParserStorageConnector gs, boolean managingIDs) {
		return pf.registerStorage(storageName, gs, managingIDs);
	}


	/**
	 * static UI; later to be replaced by a web based interface
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws StorageRegistrationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, StorageRegistrationException {
		UIControl u = new UIControl();
		u.registerParser("archimate3", new Archimate3Parser());
		u.registerStorage("archimate3", new Archimate3MongoDBConnector(), true);
		u.registerStorage("archimate3", new Archimate3Neo4jConnector(), false);
//		u.registerParser("archimate", new ArchimateParser());
		//u.registerParser("bpmn", new BPMNParser());
//		u.registerParser("disco", new DiscoResultParser());
		
		// insert an archimate file into mongoDB
		mongo.dropCollections();
		graph.emptyDatabase();
		
//		Neo4jAccess ne = new Neo4jAccess();
//		ne.exportTransitionClass("export_transition_class.csv");
//		ne.exportNodeClass("export_node_class.csv");
//		ne.exportTransitions("export_transitions.csv");+
//		ne.exportNodes("export_nodes.csv");
		
//		boolean r = u.parseFile("This_example.xml");
//		boolean r = u.parseFile("thijs_example_retrieved3.xml");
//		boolean r = u.parseFile("OTK Sample.xml");
//		boolean r = u.parseFile("whr_line_6.xml");
		//boolean r = u.parseFile("disco_demo_export.xml");
//		LOGGER.info("file parsed :"+r);
//		boolean r = u.parseJsonString(jsonStr);
		
		String json = u.readFile("demo_model_v3_20170220.json");
		u.parseJsonString(json);
		
		//mongo.getAllDocuments();
		
//		LOGGER.info(u.deriveString("archimate3", new Date(System.currentTimeMillis())));
		// retrieve an archimate file into mongoDB
		Date date = new Date(System.currentTimeMillis());
//		u.deriveFile("archimate", "test_otk2.xml", date);
//		u.deriveFile("archimate3", "thijs_example_retrieved4.xml", date);
//		LOGGER.info("retrieved result");
//		LOGGER.info(u.deriveJsonString("archimate3", date));
	}

}
