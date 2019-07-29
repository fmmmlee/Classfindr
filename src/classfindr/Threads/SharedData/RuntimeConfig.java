package classfindr.Threads.SharedData;
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

import classfindr.Utility.ProgramMetricsTracker;

import static classfindr.Utility.Constants.*;
/**
 *
 * Object shared between various different threads that holds variables and other objects used by them, often concurrently
 *
 */
public class RuntimeConfig {
		
	/** output of CallServer/input of ParseDoc */
	public BlockingQueue<Document> unparsed = new LinkedBlockingQueue<Document>();
	
	/** output of ParseDoc/input of CourseConverter */
	public BlockingQueue<Course> course_queue = new LinkedBlockingQueue<Course>();
	
	/** queue for uploading to AWS in insert mode - output of CourseConvert/input of UploadToAWS */
	public BlockingQueue<HashMap<String, AttributeValue>> put_queue;
	/**queue for keys when uploading to AWS in update mode - output of CourseConvert/input of UploadToAWS */
	public BlockingQueue<HashMap<String, AttributeValue>> key_queue;
	/**queue for items when uploading to AWS in update mode - output of CourseConvert/input of UploadToAWS */
	public BlockingQueue<HashMap<String, AttributeValueUpdate>> update_queue;
	
	
	/** queue for update statements for local DB - output of CourseConvert/input of AccessLocalDB */
	private BlockingQueue<String> local_queue;
	
	
	/** size of each upload job */
	private BlockingQueue<Integer> uploadSizes = new LinkedBlockingQueue<Integer>();
	
	
	/** tracks whether server call is complete */
	public AtomicBoolean calls_finished = new AtomicBoolean(false);
	/** tracks whether html parsing is complete */
	public AtomicBoolean parse_finished = new AtomicBoolean(false);
	/** tracks whether object conversion is complete */
	public AtomicBoolean converting = new AtomicBoolean(true);
	
	
	/** batch mode - not implemented yet */
	public AtomicBoolean batch_mode = new AtomicBoolean(false);
	
	/** object that holds execution stats and information */
	public ProgramMetricsTracker metric;

	/** run preferences */
	public Preferences preferences;
	
	/**
	 * Constructor
	 * 
	 * @param mode_in the upload mode to use (sets local variable mode)
	 * @param terms_in array of strings showing all terms being processed (sets local variable terms)
	 * @param table_in name of table in destination database (sets local variable table)
	 */
	public RuntimeConfig(Preferences preferences)
	{
		this.preferences = preferences;
		metric = new ProgramMetricsTracker(preferences);
		
		/* initializing queues depending on specified mode */
		switch(preferences.database)
		{
		case EMBEDDED :
			local_queue = new LinkedBlockingQueue<String>();
			break;
		case AWS :
			switch(preferences.mode)
			{
			case 1:
				put_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
				break;
			case 2:
				key_queue = new LinkedBlockingQueue<HashMap<String, AttributeValue>>();
				update_queue = new LinkedBlockingQueue<HashMap<String, AttributeValueUpdate>>();
				break;
			}
			break;
		}		
	}

	public BlockingQueue<String> get_localQueue() {
		return local_queue;
	}

	public BlockingQueue<Integer> getUploadSizes() {
		return uploadSizes;
	}
	
}
