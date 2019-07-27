package classfindr.ExecutedThreads;
/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Convert Course objects into appropriate objects for upload to DynamoDB
 * 
 * 
 */

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;

import classfindr.ConsoleInterface.Notifications;
import classfindr.Utility.Course;
import classfindr.Utility.Metric;
import classfindr.Utility.ThreadShare;

import static classfindr.Utility.Constants.*;

public class CourseConverter implements Runnable {
	
	int mode; 		
	int destination;
	
	BlockingQueue<Course> input; //input to this thread
	
	/* outputs for AWS */
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_output;
	BlockingQueue<HashMap<String, AttributeValue>> key_output;
	BlockingQueue<HashMap<String, AttributeValue>> put_output;
	
	/* outputs for local DB */
	BlockingQueue<String> local_output;
	
	/* tracking execution status */
	AtomicBoolean still_converting;
	AtomicBoolean parse_finished;
	
	/* shared metric - this thread contributes conversion time */
	Metric thisMetric;
	
	String table_name;
	
	/* constructor */
	public CourseConverter(ThreadShare shared)
	{
		local_output = shared.get_localQueue();
		destination = shared.preferences.database;
		put_output = shared.put_queue;
		key_output = shared.key_queue;
		update_output = shared.update_queue;
		input = shared.course_queue;
		parse_finished = shared.parse_finished;
		still_converting = shared.converting;
		mode = shared.preferences.mode;
		thisMetric = shared.metric;
		table_name = shared.preferences.table;
		
	}
	
	/**
	 * Watches the input queue and if there is something on it,
	 * converts it according to specified mode and puts the output in the appropriate queue(s).
	 * Continues running until queue is empty and parse thread reports finished.
	 */
	
	public void run()
	{
		long start_time = System.nanoTime();
		int uploads = 0;
		Notifications.thread_spun("converter");
		if(destination == LOCAL)
		{
			uploads = localQueueHandler(uploads); //shouldn't need the "uploads = " part but I left it in just in case, will test remove later
		}
		else
		{
			uploads = AWS_queue(uploads);
		}
		
		thisMetric.set_conversion_time(System.nanoTime()-start_time);
		Notifications.task_finished("all conversions");
		still_converting.set(false);
		thisMetric.set_total_uploads(uploads);
		return;
	}
	
	
	/* Converting to AWS format */
	private int AWS_queue(int uploads)
	{
		while(!parse_finished.get() || input.peek() != null)
		{
			while(input.peek() != null)
			{
				try {
					switch(mode)
					{
					case 1 :
						uploads++;
						put_output.put(input.poll().generateItemPush());
						break;
					case 2 :
						Course temp_course = input.poll();
						key_output.put(temp_course.itemKey());
						update_output.put(temp_course.generateItemUpdate());
						uploads++;
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return uploads;
	}
	
	/* Converting to strings for local DB */
	private int localQueueHandler(int uploads)
	{
		while(!parse_finished.get() || input.peek() != null)
		{
			while(input.peek() != null)
			{
				try
				{
					uploads++;
					switch(mode)
					{
					case 1 :
						local_output.put(input.poll().generateLocalInsert(table_name));
						break;
					case 2 :
						local_output.put(input.poll().generateLocalUpdate(table_name));
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return uploads;
	}
	
	
}
