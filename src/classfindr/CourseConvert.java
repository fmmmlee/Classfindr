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
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;

public class CourseConvert implements Runnable {
	
	int mode;
	BlockingQueue<Course> input;
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_output;
	BlockingQueue<HashMap<String, AttributeValue>> key_output;
	BlockingQueue<HashMap<String, AttributeValue>> put_output;
	AtomicInteger still_parsing;
	AtomicInteger still_converting;
	Metric thisMetric;
	
	/* constructor */
	CourseConvert(ThreadShare shared)
	{
		put_output = shared.put_queue;
		key_output = shared.key_queue;
		update_output = shared.update_queue;
		input = shared.course_queue;
		still_parsing = shared.size;
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
		while(true) {
			if(k == still_parsing.get() && k != 0) {
				thisMetric.set_conversion_time(System.nanoTime()-start_time);
				Notifications.task_finished("conversion");
				still_converting.set(0);
				return;
			}
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
	}
	
	
	
	
}
