/**
 * 
 */
package iea.marshall.archimate3;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bson.Document;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.ParserFactory;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.iea.connector.storage.MongoDBSingleton;
import org.iea.util.DifRecord;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengroup.xsd.archimate._3.ModelType;
import org.skyscreamer.jsonassert.JSONAssert;

import com.mongodb.util.JSON;

/**
 * @author wombach
 *
 */
public class Archimate3FileIBasedInteractionTestConflicts {

	public ParserFactory pf = new ParserFactory();
	
	public Archimate3FileIBasedInteractionTestConflicts(){
		super();
		pf.registerParser("archimate3", new Archimate3Parser());
		pf.registerStorage("archimate3", new Archimate3MongoDBConnector(), true);
		//		pf.registerStorage("archimate3", new Archimate3Neo4jConnector(), false);
		//		pf.registerParser("archimate", new ArchimateParser());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		//		pf.registerParser("archimate3", new Archimate3Parser());
		//		pf.registerStorage("archimate3", new Archimate3MongoDBConnector(), true);
		//		//		pf.registerStorage("archimate3", new Archimate3Neo4jConnector(), false);
		//		//		pf.registerParser("archimate", new ArchimateParser());
//		pf.dropProject("archimate3_test_project2");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

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

	@Test
	public void givenJsonFile_store_retrieve_subtests() throws JAXBException, IOException{
//		// initial load of the model
//		givenJsonFile_processJsonString_expectTrue();
//
//		// retrieve model 
//		givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile();
//		//givenJsonFile_processJsonString_no_updates_expectTrue();
//		
//		// make changes to the retrieved model file
//		givenJsonFile_modifyJson();
//		
//		// retrieve model again 
//		givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile2();
//		
//		// make changes to the retrieved model file
//		givenJsonFile_modifyJson2();
		
		// commit modified model
//		givenJsonFile_processJsonString_expectTrue3();

		// check conflicts of modified model
		Vector<DifRecord> dif = givenJsonFile_processJsonString_expectTrue4();
		Set<String> list = new HashSet<String>();
		for(DifRecord di : dif){
			Document d = di.getLeft();
			String json = JSON.serialize( d );
			System.out.println(json);
			json = json.substring(json.indexOf("\"@identifier\" : \"")+"\"@identifier\" : \"".length());
			String uuid = json.substring(0,json.indexOf("\" ,"));
			System.out.println(uuid);
			list.add(uuid);
		}
		
		// commit modified model by keeping the node in the repository
		givenJsonFile_processJsonString_expectTrue5(list);

		// commit modified model by updating the node in the repository
//		givenJsonFile_processJsonString_expectTrue6();

		
//		givenXmlFile_UnmarshalJson_InParserFactory();
		//givenXMLFile_unmarshalJson_marshalXML();
		//givenJsonFile_processJsonString_expectTrue();
//		givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile();
//				givenJsonFile_unmarshalJson_marshalXML();
//		givenJsonFile_processJsonString_no_updates_expectTrue();
	}

	public void givenJsonFile_processJsonString_expectTrue() {		
		String json = readFile("demo_archimate3.json");
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, true, null);
		assertTrue(ret == null);	
	}

	public void givenJsonFile_processJsonString_expectTrue3() {		
		String json = readFile("test5_output_mod.json");
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, false, null);
	    System.out.println("resulting changes - conflicts "+ret.size());
		assertTrue(ret == null || ret.size()==0);	
		ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, true, null);
		assertTrue(ret == null);	
	}

	public Vector<DifRecord> givenJsonFile_processJsonString_expectTrue4() {		
		String json = readFile("test6_output_mod.json");
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, false, null);
		assertTrue(ret != null && ret.size()==1 );
		return ret;
	}

	public void givenJsonFile_processJsonString_expectTrue5(Set<String> list) {		
		String json = readFile("test6_output_mod.json");
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, true, list);
		assertTrue(ret == null || ret.size()==0 );	
	}

	public Vector<DifRecord> givenJsonFile_processJsonString_expectTrue6() {		
		String json = readFile("test6_output_mod.json");
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1", json, true, null);
		assertTrue(ret == null || ret.size()==0 );
		return ret;
	}


	public void givenJsonFile_processJsonString_no_updates_expectTrue() {		
		String json = readFile("test5_output.json");
		MongoDBSingleton.dropCollection();
		String taskId = UUID.randomUUID().toString();
		Vector<DifRecord> ret = pf.processJsonString(taskId, "archimate3_test_project2","branch1","user1",json, false, null);
	    System.out.println("resulting changes - conflicts "+ret.size());
		assertTrue(ret == null || ret.isEmpty());	
	}

	public void givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile() {		
		Date date = new Date(System.currentTimeMillis());
		String taskId = UUID.randomUUID().toString();
		String t = pf.retrieveJsonString(taskId, "archimate3", "archimate3_test_project2","branch1", "user1", date);
		Path path = Paths.get("test5_output.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception "+e);
		}
	}

	public void givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile2() {		
		Date date = new Date(System.currentTimeMillis());
		String taskId = UUID.randomUUID().toString();
		String t = pf.retrieveJsonString(taskId, "archimate3", "archimate3_test_project2","branch1", "user1", date);
		Path path = Paths.get("test6_output.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception "+e);
		}
	}

	public void givenJsonFile_modifyJson() {
		String json = readFile("test5_output.json");
		
		String t = json.replace("material deposition or modification", "material deposition or modification2");
		Path path = Paths.get("test5_output_mod.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception "+e);
		}
	}

	public void givenJsonFile_modifyJson2() {
		String json = readFile("test6_output.json");
		
		String t = json.replace("material deposition or modification", "material deposition or modification3").
						replace("@target\" : \"id-4b6e289e-210b-4aff-8ba2-b96d61cbb4eb", "@target\" : \"id-e90e8781-63e5-4b56-9c15-95e2bd908e2f");
		Path path = Paths.get("test6_output_mod.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception "+e);
		}
	}


	public void givenXmlFile_UnmarshalJson_InParserFactory() throws JAXBException, IOException{
		String xml = readFile("demo_archimate3.xml");
		String res = pf.convertXMLtoJSON("this is a test", "archimate3", xml);
	}
	
	public void givenJsonFile_unmarshalJson_marshalXML() throws JAXBException, IOException{
		JAXBContext jaxbContext =  JAXBContext.newInstance(ModelType.class);

		Map<String, String> namespaces = new HashMap<String, String>();
//		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

//		File file = new File("demo_archimate3.json");
		File file = new File("test3_output.json");
		StreamSource source = new StreamSource(file);
		Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
		JAXBElement<ModelType> result = unmarshaller2.unmarshal(source, ModelType.class);
		ModelType model = (ModelType) result.getValue();

		namespaces = new HashMap<String, String>();
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		//		jaxbContext =  JAXBContext.newInstance(ModelType.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
		//		marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
		//		marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
		//		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
		FileWriter out = new FileWriter( "test3_output.xml");
		marshaller.marshal(model, out);
		out.flush();
		out.close();
	}

	public void givenXMLFile_unmarshalJson_marshalXML() throws JAXBException, IOException{
		JAXBContext jaxbContext =  JAXBContext.newInstance(ModelType.class);

		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

		//File file = new File("test_w_properties.xml");
//		File file = new File("test3_output.xml");
		File file = new File("demo_archimate3.xml");
		StreamSource source = new StreamSource(file);
		Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
//		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
		JAXBElement<ModelType> result = unmarshaller2.unmarshal(source, ModelType.class);
		ModelType model = (ModelType) result.getValue();

		namespaces = new HashMap<String, String>();
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "");
		namespaces.put("http://www.opengroup.org/xsd/archimate/3.0/", "ar3");
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		//		jaxbContext =  JAXBContext.newInstance(ModelType.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
				marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
				marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
				marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
		FileWriter out = new FileWriter( "test_w_properties.json");
		marshaller.marshal(model, out);
		out.flush();
		out.close();
	}

}
