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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bson.Document;
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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * @author wombach
 *
 */
public class MongoDBLockingTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	private void drop(){
		MongoCollection<Document> col = getCollection();
		col.drop();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private static MongoCollection<Document> getCollection(){
		MongoClient mongo = MongoDBSingleton.getClient();
		MongoDatabase database = mongo.getDatabase(MongoDBSingleton.MANAGEMENT_DATABASE);
		return database.getCollection(MongoDBSingleton.MANAGEMENT_COLLECTION);
	}
	
	@Test
	public void givenEmptyStore_addMultipleLocks_checkAvailabilityOfLocks(){
		drop();
		assertTrue(MongoDBSingleton.getLock("testProject", "branch1", "user1", "model_id_1", 500));
		MongoDBSingleton.releaseLock("testProject", "branch1", "user1");
		assertTrue(MongoDBSingleton.getLock("testProject", "branch1", "user1", "model_id_1", 500));
		assertFalse(MongoDBSingleton.getLock("testProject", "branch1", "user1", "model_id_1", 500));
		assertTrue(MongoDBSingleton.getLock("testProject", "branch2", "user1", "model_id_1", 500));
		MongoDBSingleton.releaseLock("testProject", "branch2", "user1");
		MongoDBSingleton.releaseLock("testProject", "branch1", "user1");
		
	}
	
}
