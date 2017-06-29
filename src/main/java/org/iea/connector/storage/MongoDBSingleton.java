package org.iea.connector.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoDBSingleton {
	public static final String DOC_BRANCHES = "branches";
	public static final String DOC_START_TIME = "start_time";
	public static final String DOC_MODEL_ID = "model_id";
	public static final String DOC_LOCK = "lock";
	public static final String DOC_USER = "user";
	public static final String DOC_BRANCH = "branch";
	public static final String DOC_PROJECT = "project";
	public final static String MANAGEMENT_DATABASE = "management";
	public final static String MANAGEMENT_COLLECTION = "management";

	private static MongoClient mongoClient = null;
	private final static Logger LOGGER = Logger.getLogger(MongoDBSingleton.class.getName()); 
	
	private static HashMap<String, HashMap<String, Boolean>> lock = loadLock();
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

	private static HashMap<String, HashMap<String, Boolean>> loadLock() {
		HashMap<String, HashMap<String, Boolean>> ret = new HashMap<String, HashMap<String, Boolean>>();
		HashMap<String, Boolean> proj;
		MongoCollection<Document> col = getCollection();
		FindIterable<Document> res = col.find();
		MongoCursor<Document> it = res.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			String project = doc.getString(DOC_PROJECT);
			if(ret.containsKey(project)){
				proj = ret.get(project);
			} else {
				proj = new HashMap<String, Boolean>();
				ret.put(project, proj);
			}
			ArrayList<Document> branches = (ArrayList<Document>) doc.get(DOC_BRANCHES);
			for(int ii=0;ii<branches.size();ii++){
				Document elm = branches.get(ii);
				String branch = elm.getString(DOC_BRANCH);
				boolean flag = elm.getBoolean(DOC_LOCK, false);
				proj.put(branch, flag);
			}
		}
		return ret;
	}
	
	public static void releaseLock(String project, String branch, String user){
		if(lock.containsKey(project)){
			HashMap<String, Boolean> proj = lock.get(project);
			if( proj.containsKey(branch)){
				boolean flag = proj.get(branch);
				if(flag == true){
					proj.replace(branch, false);
					updateLock(project, branch, user, null, -1, false);
				} else {
					// no lock set
				}
			} else {
				// no branch
				LOGGER.warning("something went wrong with registering the lock for project:"+project+"    branch:"+branch);
			}
		} else {
			// new project
			LOGGER.warning("something went wrong with registering the lock for project:"+project+"    branch:"+branch);
		}
	}

	public static boolean getLock(String project, String branch, String user, String model_id, long time){
		boolean ret = false;
		if(lock.containsKey(project)){
			HashMap<String, Boolean> proj = lock.get(project);
			if( proj.containsKey(branch)){
				boolean flag = proj.get(branch);
				if(flag == false){
					proj.replace(branch, true);
					updateLock(project, branch, user, model_id, time, true);
					ret = true;
				} else {
					// lock is taken by somebody else
					ret = false;
				}
			} else {
				// new branch
				insertBranchLock(project, branch, user, model_id, time);
				proj.put(branch, true);
				ret = true;
			}
		} else {
			// new project
			inserProjectLock(project, branch, user, model_id, time);
			HashMap<String, Boolean> proj = new HashMap<String, Boolean>();
			proj.put(branch, true);
			lock.put(project, proj);
			ret = true;
		}
		return ret;
	}


	private static void inserProjectLock(String project, String branch, String user, String model_id, long time) {
		MongoCollection<Document> collection = getCollection();
		Document doc = new Document(DOC_PROJECT,project);
		ArrayList<Document> branches = new ArrayList<Document>();
		Document br = new Document(DOC_BRANCH, branch);
		br.append(DOC_USER, user).append(DOC_LOCK, true).append(DOC_MODEL_ID, model_id).append(DOC_START_TIME, time);
		branches.add(br);
		doc.append(DOC_BRANCHES, branches );
		collection.insertOne(doc);
		LOGGER.info("collection count count after insert: "+collection.count());		
	}

	private static MongoCollection<Document>getCollection(){
		MongoClient mongo = getClient();
		MongoDatabase database = mongoClient.getDatabase(MANAGEMENT_DATABASE);
		return database.getCollection(MANAGEMENT_COLLECTION);
	}
	
	public static void dropCollection(){
		MongoCollection<Document> col = getCollection();
			col.drop();
	}
	
	private static void insertBranchLock(String project, String branch, String user, String model_id, long time) {
		MongoCollection<Document> collection = getCollection();
		Document doc = new Document(DOC_PROJECT,project);
		Document br = new Document(DOC_BRANCH, branch);
		br.append(DOC_USER, user).append(DOC_LOCK, true).append(DOC_MODEL_ID, model_id).append(DOC_START_TIME, time);
		Document branches = new Document(DOC_BRANCHES,br);
		Document set = new Document("$addToSet",branches); 
		collection.updateOne(doc, set);
		//LOGGER.info("collection count count after insert: "+collection.count());		
	}

	private static void updateLock(String project, String branch, String user, String model_id, long time, boolean flag) {
		MongoCollection<Document> collection = getCollection();
		Document doc = new Document(DOC_PROJECT,project).append(DOC_BRANCHES+"."+DOC_BRANCH, branch);
		Document br = new Document(DOC_BRANCHES+".$."+DOC_USER, user);
		br.append(DOC_BRANCHES+".$."+DOC_LOCK, flag).append(DOC_BRANCHES+".$."+DOC_MODEL_ID, model_id).append(DOC_BRANCHES+".$."+DOC_START_TIME, time);
		Document set = new Document("$set",br); 
		collection.updateOne(doc, set);
		//LOGGER.info("collection count count after insert: "+collection.count());				
	}

}
