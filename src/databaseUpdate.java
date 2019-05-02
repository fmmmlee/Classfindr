/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Update database with information from ParseClassfinderDoc
 * 
 * Works with proper credentials set up on the Amazon CLI
 * 
 */

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;

public class databaseUpdate{

    public static void main(String[] args) throws IOException{


    	 /* Each course also has a little option to view information about it, another POST request with just the CRN and a few hidden inputs. */
    	
    	//eventually will need to build update queries based on current term and analysis queries for old terms
    	//createCourseTable();
    	fullTermUpdate("course", "201920");
    	
    }

    //creates table with name "course"
    private static void createCourseTable()
    {
    	CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                "crn", ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement("crn", KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(
                new Long(10), new Long(10)))
                .withTableName("course");
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        try {
            CreateTableResult result = ddb.createTable(request);
            System.out.println(result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("successfully created table with name course");
    }
    
    private static void fullTermUpdate(String tableName, String term) throws IOException
    {
    	Document unsorted = CallUniServer.fullTermQuery(term);
    	File testfile = new File("testingSize.html");
    	FileUtils.writeStringToFile(testfile, unsorted.outerHtml(), "UTF-8");
    	//Document unsorted = Jsoup.parse(new File("testclean.html"), "UTF-8");
    	List<HashMap<String, AttributeValue>> updateWithThis = ParseClassfinderDoc.parseDocument(unsorted, term);
    	
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

    	for(HashMap<String, AttributeValue> tableEntry : updateWithThis)
    	{
    		try {
                ddb.putItem(tableName, tableEntry);
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The table \"%s\" can't be found.\n", tableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
                System.exit(1);
            } catch (AmazonServiceException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
    	}
    	System.out.println("successfully updated table with name " + tableName);
    }

}