package org.iea.connector.storage;

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
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;

public class Neo4jSingleton {
	private final static Logger LOGGER = Logger.getLogger(Neo4jSingleton.class.getName());

	private static Driver driver = null;

	public static Session getSession(){
		Session session = null;
		if(driver==null){
			driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wipro@123" ) );
		}
		session = driver.session();
		return session;
	}


}
