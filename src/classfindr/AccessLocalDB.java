package classfindr;

/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Update local embedded database with information from ParseDoc
 * 
 * 
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccessLocalDB implements Runnable
{
	/* Progress Bar */
	static final int pbar_size = 60;	//number of ticks in progress bar
	static boolean begun_bar;			//says whether progress bar has started printing or not
	
	/* Database */
	static final String driver = "org.h2.Driver";		//database driver
	static final String DB_URL = "jdbc:h2:~/test";		//database connection URL
	static final String DB_USER = "sa";					//username for database
	static final String DB_PASS = "";					//password for database
	static Connection db_conn;							//connection to the database
	static String table;								//table to access/create/modify
	
	/* Queue Processing */
	BlockingQueue<String> incoming_queries;		//queue of string queries
	static BlockingQueue<Integer> job_size;		//queue that holds individual job sizes
	static int job_progress;					//tracks progress of current database task
	String[] terms; 							//school terms being worked with
	String current_term;						//term being currently processed
	
	/* Thread Communication */
	AtomicBoolean finished_converting;	//indicates if converter thread has finished
	
	/* Metrics */
	Metric thisMetric;			//shared metric object, used to record upload stats
	long start_time; 			//records start time
	static double this_second; 	//tracking statements processed in the current second
	double per_second; 			//average statements per second
	List<Double> second_list; 	//list of statements processed for each second
	
		
	/* constructor */
	public AccessLocalDB(ThreadShare shared)
	{
		incoming_queries = shared.local_queue;
		terms = shared.terms;
		table = shared.table;
		job_size = shared.upload_sizes;
		finished_converting = shared.converting;
		thisMetric = shared.metric;
	}
	
	
	public void run()
	{
		Notifications.thread_spun("upload");
		try {
			db_conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			handle_queue();
		} catch (SQLException e) {
			e.printStackTrace();
			return;	
		}
	}
	
	
	
	/*
	 * 
	 * takes strings from the queue (written to by CourseConverter) and executes them as queries to the database target
	 * 
	 */
	private void handle_queue() throws SQLException
	{
		int j = 0;	//index in array of terms to set current_term to
		begun_bar = true;
		long start_second = 0;
		this_second = 0.0;
		per_second = 0.0;
		job_progress = 0;
		second_list = new ArrayList<Double>();
		start_time = System.nanoTime();
		current_term = terms[j];
		
		/* if destination table does not exist, create it */
		if(!LocalDBTools.table_exist(table, db_conn))
		{
			LocalDBTools.create_course_table(table, db_conn);
		}
		
		/* loop to handle queue, returns when finished */
		while(true) {	
			////////////////METRICS/////////////////
			if(start_second == 0)
				start_second = System.nanoTime();
			else if(System.nanoTime() - start_second > 1000000000)
			{
				second_list.add(this_second);
				this_second = 0.0;
				start_second = System.nanoTime();
			}
			////////////////////////////////////////
			
			if(newTermHandler(j) == 1)
				return;	

			while(incoming_queries.peek() != null)
			{
				if(newTermHandler(j) == 1)
					return;				
				
				if(System.nanoTime() - start_second > 1000000000)
				{
					second_list.add(this_second);
					this_second = 0;
					start_second = System.nanoTime();
				}
				
				exec_statement();
			}
		}
	}
	
	
	
	/******* SQL and progress bar wrapper *******/
	private void exec_statement() throws SQLException
	{
		Statement newstatement;
		newstatement = db_conn.createStatement();
		newstatement.execute(incoming_queries.peek());
		Waiting_Indicators.progress_bar(pbar_size, job_progress, job_size.peek(), begun_bar, current_term);
		job_progress++;
		this_second++;
		
	}
	
	
	
	
	/******* QUEUE HANDLER HELPER METHODS *******/

	/*
	 * checks if the current term number is outside the bounds of the terms array
	 */
	private boolean isLastTerm(int termNum)
	{
		if(termNum >= terms.length)
		{
			thisMetric.add_upload_time(System.nanoTime()-start_time);
			for(double second : second_list)
			{
				per_second += second;
			}
			per_second = per_second/second_list.size();
			thisMetric.add_upload_rate(per_second);
			return true;
		}
		return false;
	}
	
	/*
	 * 
	 * Takes care of variables if finished processing courses in the current term. If it is the last term, returns 1, otherwise 0.
	 * 
	 */
	private int newTermHandler(int j)
	{
		if(job_size.peek() != null && job_size.peek() == job_progress)
		{
			job_size.poll(); //removing the size of the completed job from the queue
			job_progress = 0;
			j++;
			
			if(isLastTerm(j))
				return 1;
			
			current_term = terms[j];
		}
		return 0;
	}
}
