package org.idw.storage.connector;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class Neo4jAccess {
	private final static Logger LOGGER = Logger.getLogger(Neo4jAccess.class.getName());

	private Driver driver = null;

	public Session getSession(){
		Session session = null;
		if(driver==null){
			driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wipro@123" ) );
		}
		session = driver.session();
		return session;
	}

	public void emptyDatabase(){
		String query = "match (n) DETACH DELETE n;";
		try(Session session = getSession()){
			try ( Transaction tx = session.beginTransaction() ){
				tx.run( query);
				tx.success();
			}
		}
		LOGGER.warning("graph database has been emptied");
	}

	public void exportTransitions(String filename) throws IOException{
		Session session = getSession();
		FileWriter fw = new FileWriter(filename);
		StatementResult result = session.run( "MATCH (s)-[r:archimate3_has_target]->(t) "+
		"RETURN s.identifier as sourceUUID,s.nodeType as sourceType, s.name as sourceName ,"+
				"t.nodeType as targetType, t.identifier as targetUUID, t.name as targetName" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    System.out.println( record.get( "sourceUUID" ).asString() + "," + record.get( "sourceType" ).asString()+
		    		"," + record.get( "sourceName" ).asString()+"," + record.get( "targetUUID" ).asString()+
		    		"," + record.get( "targetType" ).asString()+"," + record.get( "targetName" ).asString());
		    fw.write( record.get( "sourceUUID" ).asString() + "," + record.get( "sourceType" ).asString()+
		    		"," + record.get( "sourceName" ).asString()+"," + record.get( "targetUUID" ).asString()+
		    		"," + record.get( "targetType" ).asString()+"," + record.get( "targetName" ).asString()+"\n");
		}
		session.close();
		fw.close();
	}

	public void exportNodes(String filename) throws IOException{
		Session session = getSession();
		FileWriter fw = new FileWriter(filename);
		StatementResult result = session.run( "MATCH (n:archimate3) RETURN n.identifier as id, n.nodeType as type, n.name as name;" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    System.out.println( record.get( "id" ).asString() + "," + record.get( "type" ).asString()+
		    		"," + record.get( "name" ).asString());
		    fw.write( record.get( "id" ).asString() + "," + record.get( "type" ).asString()+
		    		"," + record.get( "name" ).asString()+"\n");
		}
		session.close();
		fw.close();
	}

	public void exportTransitionClass(String filename) throws IOException{
		Session session = getSession();
		FileWriter fw = new FileWriter(filename);
		StatementResult result = session.run( "MATCH (s)-[r1:archimate3_is_source]->(r)-[r2:archimate3_has_target]->(t)"+
			"RETURN s.nodeType as sourceNode,r.nodeType as relationNode,t.nodeType as targetNode, count(*) as cnt" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    System.out.println( record.get( "sourceNode" ).asString() + "," + record.get( "relationNode" ).asString()+
		    		"," + record.get( "targetNode" ).asString()+"," + record.get( "cnt" ).asInt());
		    fw.write( record.get( "sourceNode" ).asString() + "," + record.get( "targetNode" ).asString()+
		    		"," + record.get( "relationNode" ).asString()+"," + record.get( "cnt" ).asInt()+"\n");
		}
		session.close();
		fw.close();
	}

	public void exportNodeClass(String filename) throws IOException{
		Session session = getSession();
		FileWriter fw = new FileWriter(filename);
		StatementResult result = session.run( "MATCH (n:archimate3) RETURN n.nodeType as type, count(*) as cnt;" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    System.out.println( record.get( "type" ).asString()+
		    		"," + record.get( "cnt" ).asInt());
		    fw.write( record.get( "type" ).asString()+
		    		"," + record.get( "cnt" ).asInt()+"\n");
		}
		session.close();
		fw.close();
	}

	public void insertContent(JAXBElement content) {
		try(Session session = getSession()){
			try ( Transaction tx = session.beginTransaction() ){
				tx.run( "CREATE (a:Person {name: {name}, title: {title}})",
						parameters( "name", "Arthur", "title", "King" ) );
				tx.success();
			}
			try ( Transaction tx = session.beginTransaction() ){
				StatementResult result = tx.run( "MATCH (a:Person) WHERE a.name = {name} " +
						"RETURN a.name AS name, a.title AS title",
						parameters( "name", "Arthur" ) );
				while ( result.hasNext() ){
					Record record = result.next();
					System.out.println( String.format( "%s %s", record.get( "title" ).asString(), record.get( "name" ).asString() ) );
				}
				/**
				* create a relationship
				* MATCH (a:Person),(b:Person)
				* WHERE a.name = 'Node A' AND b.name = 'Node B'
				* CREATE (a)-[r:RELTYPE { name: a.name + '<->' + b.name }]->(b)
				* RETURN r
				*/
			}
		}

		driver.close();

	}	
	
	public void insertNode(String query) {
		try(Session session = getSession()){
			try ( Transaction tx = session.beginTransaction() ){
				StatementResult res = tx.run( query);
				tx.success();
				LOGGER.info("node inserted: "+res.toString());
			}
//			session.close();
		}
	}

	public void insertRelation(String query) {
		try(Session session = getSession()){
			try ( Transaction tx = session.beginTransaction() ){
				StatementResult res = tx.run( query);
				tx.success();
				LOGGER.info("node inserted: "+res.toString());
			}
		}
	}

	public StatementResult query(String query) {
		StatementResult res = null;
		try(Session session = getSession()){
			res = session.run( query);
			LOGGER.info("query executed");
			session.close();
		}
		return res;
	}

}
