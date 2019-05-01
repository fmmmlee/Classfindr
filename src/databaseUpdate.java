/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Update database with information from ParseClassfinderDoc
 * 
 */

import java.io.IOException;
import java.sql.SQLException;
import org.jsoup.nodes.Document;

public class databaseUpdate{

    public static void main(String[] args) throws IOException, SQLException {

    /*
     Do the below every [time interval] depending on how intensive it is and how often database needs updates
     (if making queries to University database in realtime for tools that already exist, and only doing building and data analytics with this database, latency becomes less of an issue)
     
    	make call
    	
    	parse file
    	
    	feed data into database
    	
    */
    	
    	Document unsorted = CallUniServer.defaultQuery();
    	ParseClassfinderDoc.parseDocument(unsorted);
    	
    }


}