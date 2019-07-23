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

/**
 *
 * Object shared between various different threads that holds variables and other objects used by them, often concurrently
 *
 */
public class ThreadShare {
	
	/** output of CallServer/input of ParseDoc */
	BlockingQueue<Document> unparsed = new LinkedBlockingQueue<Document>();
	
	/** output of ParseDoc/input of CourseConverter */
	BlockingQueue<Course> course_queue = new LinkedBlockingQueue<Course>();
	
	/** queue for uploading to AWS in insert mode - output of CourseConvert/input of UploadToAWS */
	BlockingQueue<HashMap<String, AttributeValue>> put_queue;
	/**queue for keys when uploading to AWS in update mode - output of CourseConvert/input of UploadToAWS */
	BlockingQueue<HashMap<String, AttributeValue>> key_queue;
	/**queue for items when uploading to AWS in update mode - output of CourseConvert/input of UploadToAWS */
	BlockingQueue<HashMap<String, AttributeValueUpdate>> update_queue;
	
	
	/** queue for update statements for local DB - output of CourseConvert/input of AccessLocalDB */
	BlockingQueue<String> update_local;
	/** queue for insert statements for local DB - output of CourseConvert/input of AccessLocalDB */
	BlockingQueue<String> put_local;
	
	
	/** size of each upload job */
	BlockingQueue<Integer> size = new LinkedBlockingQueue<Integer>();
	
	/** indicates update or insert */
	int mode;
	
	/** indicates type of destination database (AWS or local) */
	int database_type;
	
	
	/** tracks whether server call is complete */
	AtomicBoolean calls_finished = new AtomicBoolean(false);
	/** tracks whether html parsing is complete */
	AtomicBoolean parse_finished = new AtomicBoolean(false);
	/** tracks whether object conversion is complete */
	AtomicBoolean converting = new AtomicBoolean(true);
	
	
	/** batch mode - not implemented yet */
	AtomicBoolean batch_mode = new AtomicBoolean(false);
	
	/** array with all terms that will be uploaded */
	String[] terms;
	/** name of table in destination database */
	String table;
	/** object that holds execution stats and information */
	Metric metric;

	/**
	 * Constructor
	 * 
	 * @param mode_in the upload mode to use (sets local variable mode)
	 * @param terms_in array of strings showing all terms being processed (sets local variable terms)
	 * @param table_in name of table in destination database (sets local variable table)
	 */
	public ThreadShare(int mode_in, String[] terms_in, String table_in)
	{
		mode = mode_in;
		terms = terms_in;
		table = table_in;
		metric = new Metric(terms_in, table_in);
		
		//TODO: Add additional switch/conditional based on database_type and initialize queues based on that
		
		
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
