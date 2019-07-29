package classfindr.Threads.DatabaseAccess;
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

/* credit to the AWS documentation and example code on Github for most of the AWS connection bits */

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
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
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import classfindr.ConsoleInterface.Notifications;
import classfindr.ConsoleInterface.ProgressBarsEtc;
import classfindr.Threads.SharedData.RuntimeConfig;
import classfindr.Utility.ProgramMetricsTracker;

import java.lang.Math.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;

@SuppressWarnings("unused")
public class UploadToAWS implements Runnable{

	static final String HEADER = "Amazon DynamoDB Upload Initiated";
	static final int SIZE = 60;
	
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_input;
	BlockingQueue<HashMap<String, AttributeValue>> key_input;
	BlockingQueue<HashMap<String, AttributeValue>> put_input;
	static BlockingQueue<Integer> job_size;
	static int job_progress;
	
	int upload_mode;
	AtomicBoolean batch_mode;
	AtomicBoolean finished_converting;
	
	long start_second;
	static double this_second;
	double per_second;
	List<Double> second_list;
	
	String terms[];
	static String currentTerm;
	String table;
	static boolean begun_bar;
	ProgramMetricsTracker thisMetric;
	
	//TODO write interface for common AWS SDK functions, then implement here
	
	//progress bar currently takes in a boolean to decide whether to print a message or not
	//it's hacky, unnecessary and I need to change it at some point
	
	//NO BATCH SUPPORT IN THIS VERSION
	public UploadToAWS(RuntimeConfig shared)
	{
		upload_mode = shared.preferences.mode;
		batch_mode = shared.batch_mode;
		update_input = shared.update_queue;
		key_input = shared.key_queue;
		put_input = shared.put_queue;
		terms = shared.preferences.terms;
		table = shared.preferences.table;
		job_size = shared.getUploadSizes();
		finished_converting = shared.converting;
		thisMetric = shared.metric;
	}
	
	//note that the upload progress bar may appear already partly filled because the thread has been uploading before the parse and conversion jobs were complete
	public void run()
	{
		Notifications.thread_spun("upload");
		handle_queue();
	}
	
	/**upload incoming objects from queue**/
	private void handle_queue()
	{
		begun_bar = true;
		start_second = 0;
		this_second = 0.0;
		per_second = 0.0;
		job_progress = 0;
		second_list = new ArrayList<Double>();
		long start_time = System.nanoTime();
		int j = 0;
		currentTerm = terms[j];
		while(true) {
			/**setting second to count uploads per second**/
			if(start_second == 0)
				start_second = System.nanoTime();
			else if(System.nanoTime() - start_second > 1000000000)
			{
				second_list.add(this_second);
				this_second = 0.0;
				start_second = System.nanoTime();
			}	
			/**if the size of the job is equal to the current upload count, and they're not 0, return**/
			//currently nonfunctional
			if(job_size.peek() != null && job_size.peek() == job_progress)
			{
				job_size.poll(); //removing the size of the completed job from the queue
				job_progress = 0;
				j++;
				
				if(j >= terms.length) {
					thisMetric.add_upload_time(System.nanoTime()-start_time);
					for(double second : second_list)
					{
						per_second += second;
					}
					per_second = per_second/second_list.size();
					thisMetric.add_upload_rate(per_second);
					return;
				}
				currentTerm = terms[j];
			}

			
			/**use some upload method depending on upload mode**/
				switch(upload_mode)
				{
				case 1 :
					while(put_input.peek() != null) {
						if(start_second == 0)
							start_second = System.nanoTime();
						else if(System.nanoTime() - start_second > 1000000000)
						{
							second_list.add(this_second);
							this_second = 0;
							start_second = System.nanoTime();
						}
						item_put(table, put_input.poll());
					}
				case 2 :
					while(update_input.peek() != null && key_input.peek() != null)
					{
						
						//write method to do this new-term check
						if(job_size.peek() != null && job_size.peek() == job_progress)
						{
							job_size.poll(); //removing the size of the completed job from the queue
							job_progress = 0;
							j++;
							
							if(j >= terms.length) {
								thisMetric.add_upload_time(System.nanoTime()-start_time);
								for(double second : second_list)
								{
									per_second += second;
								}
								per_second = per_second/second_list.size();
								thisMetric.add_upload_rate(per_second);
								return;
							}
							currentTerm = terms[j];
						}
						
						if(System.nanoTime() - start_second > 1000000000)
						{
							second_list.add(this_second);
							this_second = 0;
							start_second = System.nanoTime();
						}
						item_update(table, key_input.poll(), update_input.poll());
					}
				case 3 :
					
				case 4 :	
				
				}
		}
	}
	

	
	/* placing a single item into the database */
    private static void item_put(String tableName, HashMap<String, AttributeValue> toPush)
    {
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    		try {
                ddb.putItem(tableName, toPush);
                job_progress++;
                this_second++;
                if(job_size.peek() != null){
                	ProgressBarsEtc.progress_bar(SIZE, job_progress, job_size.peek(), begun_bar, HEADER, currentTerm);
                	begun_bar = false;
                }
            } catch (ResourceNotFoundException e) {
            	System.err.format("Error: Table \"%s\" not found in the AWS account configured.\n", tableName);
                System.exit(1);
            } catch (AmazonServiceException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
    }
    
    /* updating one item in the database */    
    private static void item_update(String tableName, HashMap<String, AttributeValue> key, HashMap<String, AttributeValueUpdate> updates)
    {
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    		try {
                ddb.updateItem(tableName, key, updates);
                job_progress++;
                this_second++;
                if(job_size.peek() != null){
                	ProgressBarsEtc.progress_bar(SIZE, job_progress, job_size.peek(), begun_bar, HEADER, currentTerm);
                	begun_bar = false;
                }
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: Table \"%s\" not found in the AWS account configured.\n", tableName);
                System.exit(1);
            } catch (AmazonServiceException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
    }
    

    /* creates table with name "course" */
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
        System.out.println("Successfully created table with name " + tableName);
    }
}