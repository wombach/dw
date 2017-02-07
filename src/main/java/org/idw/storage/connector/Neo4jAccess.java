package org.idw.storage.connector;

import static org.neo4j.driver.v1.Values.parameters;

import javax.xml.bind.JAXBElement;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class Neo4jDBAccess {

	private Driver driver = null;

	public Session getSession(){
		Session session = null;
		if(driver==null){
			driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wipro@123" ) );
		}
		session = driver.session();
		return session;
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

}
