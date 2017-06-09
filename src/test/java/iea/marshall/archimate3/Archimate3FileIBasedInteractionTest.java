/**
 * 
 */
package iea.marshall.archimate3;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.ParserFactory;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author wombach
 *
 */
public class Archimate3FileIBasedInteractionTest {

	public ParserFactory pf = new ParserFactory();

	public Archimate3FileIBasedInteractionTest(){
		super();
		pf.registerParser("archimate3", new Archimate3Parser());
		pf.registerStorage("archimate3", new Archimate3MongoDBConnector(), true);
		//		pf.registerStorage("archimate3", new Archimate3Neo4jConnector(), false);
		//		pf.registerParser("archimate", new ArchimateParser());
		pf.dropProject("archimate3_test_project");
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
	public void givenJsonFile_store_retrieve_subtests(){
		givenJsonFile_processJsonString_expectTrue();
		givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile();
	}
	
	public void givenJsonFile_processJsonString_expectTrue() {		
		String json = readFile("demo_archimate3.json");
		assertTrue(pf.processJsonString("archimate3_test_project","branch1",json));	
	}

	public void givenProjectBranchInMongoDB_retrieveJsonString_expectContentMatchesFile() {		
		String json = readFile("demo_archimate3.json");
		Date date = new Date(System.currentTimeMillis());
		String t = pf.retrieveJsonString("archimate3", "archimate3_test_project","branch1", date);
		//pf.deriveFile("archimate3","test_project","branch2", "test3_retrieved.xml", date);
		//Get the file reference
		Path path = Paths.get("test3_output.json");

		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
		{
			writer.write(t);
			writer.flush();
			writer.close();
			File file2 = new File("test3_output.json");
			String content2 = new Scanner(file2).useDelimiter("\\Z").next();
			try {
				JSONAssert.assertEquals(json, content2, false);
			} catch (JSONException e) {
				fail("Should not have thrown any exception");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception "+e);
		}
		
	}
}
