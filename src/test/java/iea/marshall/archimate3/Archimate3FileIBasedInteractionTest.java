/**
 * 
 */
package iea.marshall.archimate3;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.ParserFactory;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author wombach
 *
 */
public class Archimate3FileIBasedInteractionTest {

	public ParserFactory pf = new ParserFactory();

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
		pf.registerParser("archimate3", new Archimate3Parser());
		pf.registerStorage("archimate3", new Archimate3MongoDBConnector(), true);
		//		pf.registerStorage("archimate3", new Archimate3Neo4jConnector(), false);
		//		pf.registerParser("archimate", new ArchimateParser());
		pf.dropProject("archimate3_test_project");
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
	public void givenJsonFile_processString_expectTrue() {		
		String json = readFile("demo_archimate3.json");
		assertTrue(pf.processJsonString("archimate3_test_project","branch1",json));	
	}

}
