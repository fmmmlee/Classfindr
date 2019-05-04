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
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import software.amazon.awssdk.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;

@SuppressWarnings("unused")
public class databaseUpdate{

	static int totalEntries = 0;
		//credit to the AWS documentation and example code on Github for most of the AWS connection bits
    public static void main(String[] args) throws IOException
    {
    	 /* Each course also has a little option to view information about it, another POST request with just the CRN and a few hidden inputs. */
    	
    	//eventually will need to build update queries based on current term and analysis queries for old terms
    	
    	
    	//Apparent earliest term: 200340
    	//deBug("200410");

    	File status = new File("status.txt", "UTF-8");
    	
    	for(int i = 2009; i < 2015; i++)
    	{
    		for(int j = 10; j < 50; j+=10)
    		{
    			String term = String.valueOf(i) + String.valueOf(j);
    			deBug(term);
    			System.out.println(term);
    			updateOnly("course", ParseClassfinderDoc.parseDocument(CallUniServer.fullTermQuery(term), term));
    			System.out.println(term + " data successfully uploaded!");
    		}
    	}
 
    }

    
    //debug
    private static List<Course> deBug(String term) throws IOException
    {
    	Document unsorted = CallUniServer.fullTermQuery(term);
    	System.out.println("Download completed, parse initiated:");
    	List<Course> notherdebug = ParseClassfinderDoc.parseDocument(unsorted, term);
    	totalEntries+=notherdebug.size();
    	System.out.println(totalEntries);
    	return notherdebug;
    }
    
    
    //creates table with name "course"
    private static void createTable(String tableName, String primaryKey)
    {
    	CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                primaryKey, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(primaryKey, KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(
                new Long(10), new Long(10)))
                .withTableName(tableName);
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        try {
            CreateTableResult result = ddb.createTable(request);
            System.out.println(result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("successfully created table with name " + tableName);
    }
    
    
    //update needs to just remove the first item from the regular hashmap and use it as the item key
    private static void updateOnly(String tableName, List<Course> toConvert) throws IOException
    {
    	
    	/* Item Keys */
    	List<HashMap<String, AttributeValue>> pKeys = new ArrayList<HashMap<String, AttributeValue>>();
    	/* Item Updates */
    	List<HashMap<String, AttributeValueUpdate>> attrUpdates = new ArrayList<HashMap<String, AttributeValueUpdate>>();
    	
    	for(Course oneCourse : toConvert)
    	{
    		pKeys.add(oneCourse.itemKey());
    		attrUpdates.add(oneCourse.generateItemUpdate());
    	}
    	System.out.println("conversion complete");
    	
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    	
    	double howMany = pKeys.size();
    	for(int i = 0; i < pKeys.size(); i++) {
    		try {
        		ddb.updateItem(tableName, pKeys.get(i), attrUpdates.get(i));
        		System.out.println("[" + org.apache.commons.lang3.StringUtils.repeat('|', (int) (50*(i/howMany))) + org.apache.commons.lang3.StringUtils.repeat(' ', (int) (50*(1.0-(i/howMany)))) + "]");
        	} catch (ResourceNotFoundException e) {
        		System.err.println(e.getMessage());
        		System.exit(1);
        	} catch (AmazonServiceException e) {
        		System.err.println(e.getMessage());
        		System.exit(1);
        	}
    	}
    }

    
    //inputs a full term's worth of information into a given table
    private static void fullTermInput(String tableName, List<Course> toConvert) throws IOException
    {
    	
    	//Conversion to AttributeValue
    	List<HashMap<String, AttributeValue>> pushThese = new ArrayList<HashMap<String, AttributeValue>>();
    	for(Course tableEntry : toConvert)
    		pushThese.add(tableEntry.generateItemPush());
    	
    	double howMany = pushThese.size();
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    	
    	double i = 0;
    	for(HashMap<String, AttributeValue> tableEntry : pushThese)
    	{
    		try {
                ddb.putItem(tableName, tableEntry);
                i++;
                System.out.println("[" + org.apache.commons.lang3.StringUtils.repeat('|', (int) (50*(i/howMany))) + org.apache.commons.lang3.StringUtils.repeat(' ', (int) (50*(1.0-(i/howMany)))) + "]");
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