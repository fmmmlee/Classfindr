package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Object to hold things shared between threads
 * 
 * 
 * A lot of these fields go unused during execution, but I wanted to cut down
 * on the number of parameters I had to put into each function so this proved
 * a simple solution.
 */

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Document;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;


public class ThreadShare {
	
	BlockingQueue<Course> course_queue = new LinkedBlockingQueue<Course>();
	BlockingQueue<HashMap<String, AttributeValue>> put_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
	BlockingQueue<HashMap<String, AttributeValue>> key_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_queue = new LinkedBlockingQueue<HashMap<String, AttributeValueUpdate>>();
	int mode;
	Document unparsed;
	AtomicInteger size = new AtomicInteger(0);
	AtomicInteger converting = new AtomicInteger(1);
	volatile boolean batch_mode = false;
	volatile String term;
	volatile String table;
	Metric metric;

	public synchronized void set_document(Document input)
	{
		unparsed = input;
	}
	
	ThreadShare(int mode_in, String term_in, String table_in, Metric metric_in)
	{
		mode = mode_in;
		term = term_in;
		table = table_in;
		metric = metric_in;
	}
	
}
