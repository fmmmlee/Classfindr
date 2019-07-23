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
	
	int mode;
	BlockingQueue<Course> input;
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_output;
	BlockingQueue<HashMap<String, AttributeValue>> key_output;
	BlockingQueue<HashMap<String, AttributeValue>> put_output;
	AtomicBoolean still_converting;
	AtomicBoolean parse_finished;
	Metric thisMetric;
	
	/* constructor */
	CourseConverter(ThreadShare shared)
	{
		put_output = shared.put_queue;
		key_output = shared.key_queue;
		update_output = shared.update_queue;
		input = shared.course_queue;
		parse_finished = shared.parse_finished;
		still_converting = shared.converting;
		mode = shared.mode;
		thisMetric = shared.metric;
	}
	
	/* 
	 * just keeps watching the input queue and if there's something on it,
	 * converts it according to specified mode and puts those new objects
	 * on the appropriate output queue(s)
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
					// TODO Auto-generated catch block
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
