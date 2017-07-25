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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.ParserFactory;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.iea.connector.storage.MongoDBSingleton;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengroup.xsd.archimate._3.ModelType;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author wombach
 *
 */
public class Archimate3FileIBasedInteractionTest {

	public ParserFactory pf = new ParserFactory();
	public String taskId = UUID.randomUUID().toString();

	public Archimate3FileIBasedInteractionTest(){
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
		//		pf.dropProject("archimate3_test_project");
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
		givenXmlFile_UnmarshalJson_InParserFactory();
		//givenXMLFile_unmarshalJson_marshalXML();
		//givenJsonFile_processJsonString_expectTrue();
//		givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile();
//				givenJsonFile_unmarshalJson_marshalXML();
//		givenJsonFile_processJsonString_no_updates_expectTrue();
	}

	public void givenJsonFile_processJsonString_expectTrue() {		
		pf.dropProject("archimate3_test_project2");
		MongoDBSingleton.dropCollection();
		String json = readFile("demo_archimate3.json");
		assertTrue(pf.processJsonString(taskId, "archimate3_test_project","branch1","user1", json, false));	
	}

	public void givenJsonFile_processJsonString_no_updates_expectTrue() {		
		String json = readFile("test3_output.json");
		MongoDBSingleton.dropCollection();
		assertTrue(pf.processJsonString(taskId, "archimate3_test_project","branch1","user1",json, false));	
	}

	public void givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile() {		
		//String json = readFile("demo_archimate3.json");
		Date date = new Date(System.currentTimeMillis());
		String t = pf.retrieveJsonString(taskId, "archimate3", "archimate3_test_project","branch1", "user1", date);
		//pf.deriveFile("archimate3","test_project","branch2", "test3_retrieved.xml", date);
		//Get the file reference
		Path path = Paths.get("test3_output.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
			//			File file2 = new File("test3_output.json");
			//			String content2 = new Scanner(file2).useDelimiter("\\Z").next();
			//			try {
			//				JSONAssert.assertEquals(json, content2, false);
			//			} catch (JSONException e) {
			//				fail("Should not have thrown any exception");
			//			}
			// transform to XML
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception "+e);
		}
//		String t_xml = pf.writeJSONtoXML("archimate3", t);
//		path = Paths.get("test3_output.xml");
//
//		//Use try-with-resource to get auto-closeable writer instance
//		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
//		{
//			writer.write(t);
//			writer.flush();
//			writer.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			fail("Exception "+e);
//		}

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
