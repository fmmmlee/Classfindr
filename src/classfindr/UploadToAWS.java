package classfindr;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;

@SuppressWarnings("unused")
public class UploadToAWS implements Runnable{

	static final double SIZE = 60.0;
	
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_input;
	BlockingQueue<HashMap<String, AttributeValue>> key_input;
	BlockingQueue<HashMap<String, AttributeValue>> put_input;
	static AtomicInteger job_size;
	static int job_progress;
	
	int upload_mode;
	boolean batch_mode;
	AtomicInteger finished_converting;
	
	long start_second;
	static double this_second;
	double per_second;
	List<Double> second_list;
	
	String term;
	String table;
	static boolean begun_bar;
	Metric thisMetric;
	
	//NO BATCH SUPPORT IN THIS VERSION 
	public UploadToAWS(ThreadShare shared)
	{
		upload_mode = shared.mode;
		batch_mode = shared.batch_mode;
		update_input = shared.update_queue;
		key_input = shared.key_queue;
		put_input = shared.put_queue;
		term = shared.term;
		table = shared.table;
		job_size = shared.size;
		finished_converting = shared.converting;
		thisMetric = shared.metric;
	}
	
	//note that the upload progress bar may appear already partly filled because the thread has been uploading before the parse and conversion jobs were complete
	public void run()
	{
		
		System.out.println("### upload thread spun ###");
		handle_queue();
		return;
	}
	
	/**upload incoming objects from queue**/
	private void handle_queue()
	{
		begun_bar = false;
		start_second = 0;
		this_second = 0.0;
		per_second = 0.0;
		job_progress = 0;
		second_list = new ArrayList<Double>();
		long start_time = System.nanoTime();
		
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
			if(job_size.get() == job_progress && job_progress != 0)
			{
				thisMetric.set_upload_time(System.nanoTime()-start_time);
				for(double second : second_list)
				{
					per_second += second;
				}
				per_second = per_second/second_list.size();
				thisMetric.set_upload_rate(per_second);
				thisMetric.set_total_uploads(job_progress);
				System.out.println();
				System.out.println("                  -~-~-~ Upload Complete ~-~-~-");
				return;
			}

			
			/**use some upload method depending on upload mode**/
			if(put_input.peek() != null || update_input.peek() != null || key_input.peek() != null) {
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
					while(update_input.peek() != null || key_input.peek() != null)
					{
						//the start_second 0-catcher at the beginning of the main while loop renders these checks redundant
						if(start_second == 0)
							start_second = System.nanoTime();
						else if(System.nanoTime() - start_second > 1000000000)
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
	}
	
	/* simple console progress bar */
	//TODO: in the mid-600s / 2576 there was a hop at the end of the line; I assume it's an idiosyncrasy of the casts between double and int here
	//TODO: Change the upload progress bar to be a parameter passed in called message, for increased portability and usefulness - probably the boolean, too
	private static void progress_bar(int progress, int goal)
	{
		if(!begun_bar) {
			System.out.println("       -~-~-~-~-~ Amazon DynamoDB Upload Progress ~-~-~-~-~-       ");
			System.out.println();
    	}
		
		if(progress<goal)
			System.out.print("   ["
					+ StringUtils.repeat('|',(int) (SIZE*((double) job_progress/((double) job_size.get()))))
					+ StringUtils.repeat(' ', (int) (SIZE*(1.0-((double) job_progress/((double) job_size.get()))))) + "]" + StringUtils.repeat(" ", 6 - (int) Math.log10((double) job_progress)) + job_progress + "/" + job_size + "   \r");
		else
			System.out.println("   ["
					+ StringUtils.repeat('|',(int) (SIZE*((double) job_progress/((double) job_size.get()))))
					+ StringUtils.repeat(' ', (int) (SIZE*(1.0-((double) job_progress/((double) job_size.get()))))) + "]" + StringUtils.repeat(" ", 6 - (int) Math.log10((double) job_progress)) + job_progress + "/" + job_size);
		begun_bar = true;
	}
	
	/* placing a single item into the database */
    private static void item_put(String tableName, HashMap<String, AttributeValue> toPush)
    {
    	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    		if(job_size.get() == job_progress && job_progress != 0)
    			return;
    		try {
                ddb.putItem(tableName, toPush);
                job_progress++;
                this_second++;
                if(job_size.get() != 0){
                	progress_bar(job_progress, job_size.get());
                }
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The table \"%s\" can't be found.\n", tableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
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
    		if(job_size.get() == job_progress && job_progress != 0)
    			return;
    		try {
                ddb.updateItem(tableName, key, updates);
                job_progress++;
                this_second++;
                if(job_size.get() != 0){
                	progress_bar(job_progress, job_size.get());
                }
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The table \"%s\" can't be found.\n", tableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
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
        System.out.println("successfully created table with name " + tableName);
    }
}