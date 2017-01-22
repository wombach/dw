package org.idw.storage.connector;

import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;

import java.util.Date;

public class MongoDBAccess {
	private final static Logger LOGGER = Logger.getLogger(MongoDBAccess.class.getName()); 
	private final static String DATABASE = "test";
	public final static String COLLECTION_NODES = "nodes";
	public final static String COLLECTION_RELATIONS = "relations";
	public final static String COLLECTION_FILES = "files";
	private final MongoClient mongoClient = new MongoClient();

	public MongoCollection<Document> getCollection(String col){
		MongoDatabase database = mongoClient.getDatabase(DATABASE);
		MongoCollection<Document> collection = database.getCollection(col);
		return collection;
	}

	public void getAllDocuments(){
		MongoCollection<Document> collection = getCollection(COLLECTION_NODES);
		MongoCursor<Document> cursor = collection.find().iterator();
		LOGGER.info("Nodes");
		try {
			while (cursor.hasNext()) {
				LOGGER.info(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
		collection = getCollection(COLLECTION_RELATIONS);
		cursor = collection.find().iterator();
		LOGGER.info("Relations");
		try {
			while (cursor.hasNext()) {
				LOGGER.info(cursor.next().toJson());
			}
		} finally {
			cursor.close();
		}
		collection = getCollection(COLLECTION_FILES);
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
		MongoCollection<Document> collection = getCollection(col);
		LOGGER.info("collection count before insert: "+collection.count());
		collection.insertOne(doc);
		LOGGER.info("collection count count after insert: "+collection.count());
	}

	public void updateDocument(String col, String searchKey, String searchValue, String setKey, long time){
		MongoCollection<Document> collection = getCollection(col);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(setKey, time));

		BasicDBObject searchQuery3 = new BasicDBObject();
		searchQuery3.append(searchKey, searchValue);

		UpdateResult res = collection.updateMany(searchQuery3, updateQuery);

		LOGGER.info("update completed: matched:"+res.getMatchedCount()+"   updated:"+res.getModifiedCount());
	}

	public FindIterable<Document> queryDocument(String col, String key, String value, Date date){
		BasicDBObject query = new BasicDBObject(key, new BasicDBObject("$eq", value)).
				append("start_date",  new BasicDBObject("$gt", date.getTime())).
				append("$or", new BasicDBObject("end_date",  new BasicDBObject("$lt", date.getTime())).
						append("end_date",  new BasicDBObject("$eq", -1)));
		FindIterable<Document> iterable = getCollection(col).find(eq(key, value));
		return iterable;
	}

	public void dropCollections(){
		MongoCollection<Document> col = getCollection(COLLECTION_NODES);
		col.drop();
		col = getCollection(COLLECTION_RELATIONS);
		col.drop();
		col = getCollection(COLLECTION_FILES);
		col.drop();
	}
}
