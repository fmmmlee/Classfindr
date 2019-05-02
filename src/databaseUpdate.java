/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Update database with information from ParseClassfinderDoc
 * 
 */

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
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
    	 /* Each course also has a little option to view information about it, another POST request with just the CRN and a few hidden inputs. */
    	
    	//eventually will need to build update queries based on current term and analysis queries for old terms
    	String term = "201920";
    	Document unsorted = CallUniServer.fullTermQuery(term);
    	File testfile = new File("testingSize.html");
    	FileUtils.writeStringToFile(testfile, unsorted.outerHtml(), "UTF-8");
    	//Document unsorted = Jsoup.parse(new File("testclean.html"), "UTF-8");
    	ParseClassfinderDoc.parseDocument(unsorted, term);
    	
    }


}