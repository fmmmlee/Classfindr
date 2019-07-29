package classfindr.Threads.DatabaseAccess;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.h2.jdbcx.JdbcDataSource;

import classfindr.ConsoleInterface.Notifications;
import classfindr.ConsoleInterface.ProgressBarsEtc;
import classfindr.Threads.SharedData.RuntimeConfig;
import classfindr.Utility.ProgramMetricsTracker;

public class AccessLocalDB implements Runnable
{
	/* Progress Bar */
	final int pbar_size = 60;												//number of ticks in progress bar
	final String pbar_header = "Database Statement Execution Initiated";	//header to print over the progress bar
	boolean begun_bar;														//says whether progress bar has started printing or not
	
	/* Database */
	static final String DB_URL = "jdbc:h2:./data/classfindr";	//database connection URL
	static final String DB_USER = "sa";							//username for database
	static final String DB_PASS = "";							//password for database
	final String table;											//table to access/create/modify
	Connection db_conn;											//connection to the database		*
	
	/* Queue Processing */
	BlockingQueue<String> incomingQueries;		//queue of string queries
	BlockingQueue<Integer> jobSizes;			//queue that holds individual job sizes
	int jobProgress;							//tracks progress of current database task
	String[] terms; 							//school terms being worked with
	String currentTerm;							//term being currently processed
	
	/* Thread Communication */
	AtomicBoolean finished_converting;	//indicates if converter thread has finished
	
	/* Metrics */
	ProgramMetricsTracker thisMetric;				//shared metric object, used to record upload stats
	long uploadStartTime; 			//records start time
	long startOfSec;				//records beginning of current second being measured
	double handledCurSec; 			//tracking statements processed in the current second
	double perSecond; 				//average statements per second
	List<Double> allSecondStats;	//list of statements processed for each second
	
		
	/* constructor */
	public AccessLocalDB(RuntimeConfig shared)
	{
		incomingQueries = shared.get_localQueue();
		terms = shared.preferences.terms;
		table = shared.preferences.table;
		jobSizes = shared.getUploadSizes();
		finished_converting = shared.converting;
		thisMetric = shared.metric;
	}
	
	
	public void run()
	{
		Notifications.thread_spun("upload");
		try {
			JdbcDataSource db = new JdbcDataSource();
			db.setURL(DB_URL);
			db.setUser(DB_USER);
			db.setPassword(DB_PASS);
			db_conn = db.getConnection();
			handle_queue();
		} catch (SQLException e) {
			e.printStackTrace();
			return;	
		}
		return;
	}
	
	
	
	/*
	 * 
	 * takes strings from the queue (written to by CourseConverter) and executes them as queries to the database target
	 * 
	 */
	private void handle_queue() throws SQLException
	{
		int j = 0;	//index in array of terms to set current_term to
		begun_bar = false;
		handledCurSec = 0.0;
		perSecond = 0.0;
		jobProgress = 0;
		allSecondStats = new ArrayList<Double>();
		uploadStartTime = System.nanoTime();
		currentTerm = terms[j];

		/* if destination table does not exist, create it */
		LocalDBTools.createTable(table, db_conn);
		
		/* setting the start time of the first second of uploads */
		startOfSec = System.nanoTime();

		/* loop to handle queue, returns when finished */
		while(true) {
			perSecondTracker();
			j = newTermHandler(j);
			if(isLastTerm(j))
				return;
			
			//TODO: Check for null head of queue bug in the AWS corollary to this section of code
			while(incomingQueries.peek() != null && jobSizes.peek() != null)
			{	
				perSecondTracker();
				exec_statement();
				j = newTermHandler(j);
				if(isLastTerm(j))
					return;
			}
		}
	}
	
	
	
	/******* SQL and progress bar wrapper *******/
	private void exec_statement() throws SQLException
	{
		Statement newstatement;
		newstatement = db_conn.createStatement();
		newstatement.execute(incomingQueries.poll());
		jobProgress++;
		ProgressBarsEtc.progress_bar(pbar_size, jobProgress, jobSizes.peek(), !begun_bar, pbar_header, currentTerm);
		begun_bar = true;
		handledCurSec++;
	}
	
	
	
	
	/******* QUEUE HANDLER HELPER METHODS *******/

	/*
	 * checks if the current term number is outside the bounds of the terms array
	 */
	private boolean isLastTerm(int termNum)
	{
		if(termNum >= terms.length)
		{
			thisMetric.add_upload_time(System.nanoTime()-uploadStartTime);
			for(double second : allSecondStats)
			{
				perSecond += second;
			}
			perSecond = perSecond/allSecondStats.size();
			thisMetric.add_upload_rate(perSecond);
			return true;
		}
		return false;
	}
	
	/*
	 * 
	 * Takes care of variables if finished processing courses in the current term. If it is the last term in the current job, returns 1, otherwise 0.
	 * 
	 */
	private int newTermHandler(int j)
	{
		if(jobSizes.peek() != null && jobSizes.peek() == jobProgress)
		{
			jobSizes.poll(); //removing the size of the completed job from the queue
			jobProgress = 0;
			j++;
			if(isLastTerm(j))
				return j;	
			currentTerm = terms[j];
		}
		return j;
	}
	
	/*
	 * helper function for tracking statements processed per second
	 * TODO: Maybe make this a generic function that can record events per second
	 */
	private void perSecondTracker()
	{
		if(System.nanoTime() - startOfSec > 1000000000)
		{
			allSecondStats.add(handledCurSec);
			handledCurSec = 0;
			startOfSec = System.nanoTime();
		}
	}
}
