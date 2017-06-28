package org.iea.connector.storage;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bson.Document;
import org.iea.connector.parser.Archimate3Parser;
import org.iea.connector.parser.storage.Archimate3MongoDBConnector;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDBAccess {
	private final static Logger LOGGER = Logger.getLogger(MongoDBAccess.class.getName()); 
	private final static String DATABASE = "test";
	public final static String COLLECTION_NODES = "nodes";
	public final static String COLLECTION_VIEWS = "views";
	public final static String COLLECTION_RELATIONS = "relations";
	public final static String COLLECTION_FILES = "files";
	public final static String COLLECTION_MANAGEMENT = "management";
	public static final String COLLECTION_ORGANIZATIONS = "organizations";

	public MongoClient getClient(){
		return MongoDBSingleton.getClient();
	}

	public MongoCollection<Document> getCollection(String col){
		MongoClient mongoClient = getClient();
		MongoDatabase database = mongoClient.getDatabase(DATABASE);
		MongoCollection<Document> collection = database.getCollection(col);
		return collection;
	}

	public MongoCollection<Document> getCollection(String db ,String col){
		MongoClient mongoClient = getClient();
		MongoDatabase database = mongoClient.getDatabase(db);
		MongoCollection<Document> collection = database.getCollection(col);
		return collection;
	}

	public MongoCollection<Document> getCollection(MongoClient mongoClient, String db ,String col){
		MongoDatabase database = mongoClient.getDatabase(db);
		MongoCollection<Document> collection = database.getCollection(col);
		return collection;
	}

	public void getAllDocuments(){
		getAllDocuments(DATABASE);
	}

	public void getAllDocuments(String db){
		MongoCollection<Document> collection = getCollection(db, COLLECTION_NODES);
		MongoCursor<Document> cursor = collection.find().iterator();
		LOGGER.info("Nodes");
		try {
			while (cursor.hasNext()) {
				LOGGER.info(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
		collection = getCollection(db, COLLECTION_RELATIONS);
		cursor = collection.find().iterator();
		LOGGER.info("Relations");
		try {
			while (cursor.hasNext()) {
				LOGGER.info(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
		collection = getCollection(db, COLLECTION_FILES);
		cursor = collection.find().iterator();
		LOGGER.info("Files");
		try {
			while (cursor.hasNext()) {
				LOGGER.info(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
	}

	public void insertDocument(String col, Document doc){
		insertDocument(DATABASE, col , doc);
	}

	public void insertDocument(String project, String col, Document doc){
		MongoCollection<Document> collection = getCollection(project, col);
		//LOGGER.info("collection count before insert: "+collection.count());
		collection.insertOne(doc);
		LOGGER.info("collection count count after insert: "+collection.count());
	}

	public void updateDocument(String col, String searchKey, String searchValue, String setKey, long time){
		updateDocument(DATABASE, col, searchKey, searchValue, setKey, time);
	}

	public void updateDocument(String project, String col, String searchKey, String searchValue, String setKey, long time){
		MongoCollection<Document> collection = getCollection(project, col);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(setKey, time));

		BasicDBObject searchQuery3 = new BasicDBObject();
		searchQuery3.append(searchKey, searchValue);

		UpdateResult res = collection.updateMany(searchQuery3, updateQuery);

		LOGGER.info("update completed: matched:"+res.getMatchedCount()+"   updated:"+res.getModifiedCount());
	}

	public FindIterable<Document> queryDocument(String col, String key, String value, Date date){
		return queryDocument(DATABASE, "none", col, key, value, date);
	}

	public FindIterable<Document> queryDocument(String project, String branch, String col, String key, String value, Date date){
		return queryDocument(project, branch, col, key, value, date.getTime());
	}

	public FindIterable<Document> queryDocument(String project, String branch, String col, String key, String value, long time){
		BasicDBObject query = new BasicDBObject(key, new BasicDBObject("$eq", value)).
				append("start_date",  new BasicDBObject("$gt", time)).
				append("branch",  branch).
				append("$or", new BasicDBObject("end_date",  new BasicDBObject("$lt", time)).
						append("end_date",  new BasicDBObject("$eq", -1)));
		//		FindIterable<Document> iterable = getCollection(project, col).find(eq(key, value));
		FindIterable<Document> iterable = getCollection(project, col).find(query);
		return iterable;
	}

	/**
	 * "db."+MongoDBAccess.COLLECTION_NODES+".find({"+
	 *						"'"+DOC_BRANCH+"':\""+branch+"\","+
	 *						"'"+DOC_END_DATE+"': -1,},{'"+DOC_ID+"':true})."+
	 *						"forEach( function(myDoc){print(tojson(myDoc."+DOC_ID+")); })";	
	 * @param project
	 * @param branch
	 * @param col
	 * @return
	 */
	public Set<String> queryDocumentFindAllIds(String project, String branch, String col){
		BasicDBObject query = new BasicDBObject(Archimate3MongoDBConnector.DOC_BRANCH,  branch).
				append(Archimate3MongoDBConnector.DOC_END_DATE,  new BasicDBObject("$eq", -1));
		//		FindIterable<Document> iterable = getCollection(project, col).find(eq(key, value));
		HashSet<String> ref =  new HashSet<String>();
		//		Block<Document> extractID = new Block<Document>() {
		//			@Override
		//			public void apply(final Document document) {
		//				ref.add(document.getString(Archimate3MongoDBConnector.DOC_ID));
		//			}
		//		};
		FindIterable<Document> docs = getCollection(project, col).find(query);//.forEach(extractID);
		if(docs !=null && docs.iterator()!=null){
			MongoCursor<Document> it = docs.iterator();
			while(it.hasNext()){
				Document doc = it.next();
				ref.add(doc.getString(Archimate3MongoDBConnector.DOC_ID));
			}
		}
		return ref;
	}

	public void dropCollections(){
		dropCollections(DATABASE);
	}

	public void dropProject(String project){
		dropCollections(project);
	}

	public void dropBranch(String project, String branch){
		//dropCollections(DATABASE);
		//TODO: implement dropping all branches
	}


	public void dropCollections(String db){
		MongoCollection<Document> col = getCollection(db, COLLECTION_NODES);
		col.drop();
		col = getCollection(db, COLLECTION_RELATIONS);
		col.drop();
		col = getCollection(db, COLLECTION_FILES);
		col.drop();
		col = getCollection(db, COLLECTION_ORGANIZATIONS);
		col.drop();
		col = getCollection(db, COLLECTION_VIEWS);
		col.drop();
		col = getCollection(db, COLLECTION_MANAGEMENT);
		col.drop();
	}

	public FindIterable<Document> retrieveDocument(String project, String branch, String col, String type, long time) {
		// db.getCollection('nodes').find({'branch':"branch1", 
		//            'start_date': {$lt: 1494859350395}, 
		//            $or : [{'end_date': {$eq: -1}},
		//                    {'end_date': {$gt: 1494859350395}}]
		//           })
		BasicDBList or = new BasicDBList();
		or.add(new BasicDBObject("end_date",  new BasicDBObject("$gt", time)));
		or.add(new BasicDBObject("end_date",  new BasicDBObject("$eq", -1)));
		BasicDBObject query = new BasicDBObject("start_date",  new BasicDBObject("$lt", time)).
				append("branch",  branch).
				append("type", type).
				append("$or", or);

		FindIterable<Document> iterable = getCollection(project, col).find(query);
		return iterable;
	}
	
	public void retireDocument(String project, String col, BasicDBObject query, BasicDBObject set){
		MongoCollection<Document> colDB = getCollection(project, col);
		UpdateResult ret = colDB.updateMany(query, set);
		LOGGER.info("found :"+ret.getMatchedCount()+"   changed:"+ret.getModifiedCount());
		
	}
}
