package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Object to hold things shared between threads
 * 
 * 
 * Several of these fields go unused during execution depending on what mode is used, but I wanted to cut down
 * on the number of parameters I passed to each thread, so this proved a simple solution.
 */

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoup.nodes.Document;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;


public class ThreadShare {
	
	/* output of CallServer/input of ParseDoc */
	BlockingQueue<Document> unparsed = new LinkedBlockingQueue<Document>();
	
	/* output of ParseDoc/input of CourseConvert */
	BlockingQueue<Course> course_queue = new LinkedBlockingQueue<Course>();
	
	/* output of CourseConvert/input of UploadToAWS */
	BlockingQueue<HashMap<String, AttributeValue>> put_queue;
	BlockingQueue<HashMap<String, AttributeValue>> key_queue;
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_queue;
	
	/* size of each upload job */
	BlockingQueue<Integer> size = new LinkedBlockingQueue<Integer>();
	
	/* UPDATE vs PUT */
	int mode;
	
	/* booleans to keep track of unfinished/finished tasks */ 	//alternative to these booleans - check if thread is alive
	AtomicBoolean calls_finished = new AtomicBoolean(false);
	AtomicBoolean parse_finished = new AtomicBoolean(false);
	AtomicBoolean converting = new AtomicBoolean(true);
	
	/* batch mode - not implemented yet */
	AtomicBoolean batch_mode = new AtomicBoolean(false);
	
	/* array with all terms that will be uploaded */
	String[] terms;
	/* upload destination */
	String table;
	/* object that holds execution stats and information */
	Metric metric;

	/* constructor */
	ThreadShare(int mode_in, String[] terms_in, String table_in)
	{
		mode = mode_in;
		terms = terms_in;
		table = table_in;
		metric = new Metric(terms_in, table_in);
		
		
		/* initializing queues depending on specified mode */
		switch(mode)
		{
		case 1:
			put_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
			break;
		case 2:
			key_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
			update_queue = new LinkedBlockingQueue<HashMap<String, AttributeValueUpdate>>();
			break;
		}
		
		
	}
	
}
