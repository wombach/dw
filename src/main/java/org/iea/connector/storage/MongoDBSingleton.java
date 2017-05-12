package org.iea.connector.storage;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MongoDBSingleton {
	private final static Logger LOGGER = Logger.getLogger(MongoDBSingleton.class.getName()); 
	private final static String DATABASE = "test";
	private static MongoClient mongoClient = null;
//	private HashMap<String,MongoClient> mongoClients = new HashMap<String,MongoClient>();

//	public MongoClient getClient(String database, String userName, String password){
//		String ref = database+"|";
//		if (userName!=null && !userName.isEmpty()) ref +=userName;
//		ref+="|";
//		if (password!=null && !password.isEmpty()) ref +=password;
//		ref+="|";
//		MongoClient cl = null;
//		cl = mongoClients.get(ref);
//		if(cl == null){
//			MongoCredential credential = MongoCredential.createCredential(userName, database, password.toCharArray());
//			cl = new MongoClient(new ServerAddress(), Arrays.asList(credential));
//			if(cl!=null)
//				mongoClients.put(ref, cl);
//		}
//		return cl;
//	}
	
//	public MongoClient getClient(){
//		MongoClient mongoClient = null;
//		mongoClient = mongoClients.get("||");
//		if(mongoClient == null){
//			mongoClient = new MongoClient();
//			mongoClients.put("||", mongoClient);
//		}
//		return mongoClient;
//	}
	
	public static MongoClient getClient(){
		if (mongoClient == null){
			mongoClient = new MongoClient();
		}
		return mongoClient;
	}
	

}
