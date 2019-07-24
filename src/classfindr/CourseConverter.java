package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
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

//TODO: Add mode check (preferably on thread spin, not run) and putting items into localDB queues
public class CourseConverter implements Runnable {
	
	int mode; 			//1 = insert, 2 = update
	int destination; 	//1 = local, 2 = AWS
	
	BlockingQueue<Course> input; //input to this thread
	
	/* outputs for AWS */
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_output;
	BlockingQueue<HashMap<String, AttributeValue>> key_output;
	BlockingQueue<HashMap<String, AttributeValue>> put_output;
	
	/* outputs for local DB */
	BlockingQueue<String> update_local;
	BlockingQueue<String> insert_local;
	
	/* tracking execution status */
	AtomicBoolean still_converting;
	AtomicBoolean parse_finished;
	
	/* shared metric - this thread contributes conversion time */
	Metric thisMetric;
	
	/* constructor */
	CourseConverter(ThreadShare shared)
	{
		destination = shared.database_type;
		put_output = shared.put_queue;
		key_output = shared.key_queue;
		update_output = shared.update_queue;
		input = shared.course_queue;
		parse_finished = shared.parse_finished;
		still_converting = shared.converting;
		mode = shared.mode;
		thisMetric = shared.metric;
	}
	
	/**
	 * Watches the input queue and if there is something on it,
	 * converts it according to specified mode and puts the output in the appropriate queue(s).
	 * Continues running until queue is empty and parse thread reports finished.
	 */
	
	public void run()
	{
		long start_time = System.nanoTime();
		int k = 0;
		Notifications.thread_spun("converter");
		while(!parse_finished.get() || input.peek() != null) {
			while(input.peek() != null)
			{
				try {
					switch(mode)
					{
					case 1 :
						k++;
						put_output.put(input.poll().generateItemPush());
						break;
					case 2 :
						Course temp_course = input.poll();
						key_output.put(temp_course.itemKey());
						update_output.put(temp_course.generateItemUpdate());
						k++;
						break;
					case 3 :
						
					case 4 :
						
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		thisMetric.set_conversion_time(System.nanoTime()-start_time);
		Notifications.task_finished("all conversions");
		still_converting.set(false);
		thisMetric.set_total_uploads(k);
		return;
	}
	
	
	
	
}
