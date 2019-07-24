package classfindr;

/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Update database with information from ParseDoc
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
	
	static final int SIZE = 60;
	
	BlockingQueue<String> update_input;
	BlockingQueue<String> put_input;
	static BlockingQueue<Integer> job_size;
	static int job_progress;
	
	/* Upload Mode
	 * 1 for insert, 2 for update*/
	int upload_mode;
	
	/*
	 * Indicates if converter thread has finished 
	 */
	AtomicBoolean finished_converting;
	
	long start_second; //logs start second
	static double this_second; //tracking current second
	double per_second; //statements executed per second
	List<Double> second_list; //list of statements processed for each second
	String[] terms; //terms being worked with
	
	String table; //table to access/create/modify
	static boolean begun_bar;
	Metric thisMetric;
	
	Connection db_conn;
	
	/* constructor */
	public AccessLocalDB(ThreadShare shared)
	{
		//update_input
		//put_input
		upload_mode = shared.mode;
		terms = shared.terms;
		table = shared.table;
		job_size = shared.size;
		finished_converting = shared.converting;
		thisMetric = shared.metric;
	}
	
	public void run()
	{
		Notifications.thread_spun("upload");
		try {
			db_conn = openConn();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		handle_queue();
	}
	
	
	/**upload incoming objects from queue**/
	@SuppressWarnings("unused")
	private void handle_queue()
	{
		begun_bar = true;
		start_second = 0;
		this_second = 0.0;
		per_second = 0.0;
		job_progress = 0;
		second_list = new ArrayList<Double>();
		long start_time = System.nanoTime();
		int j = 0;
		String currentTerm = terms[j];
		int b = 0; //debug
		while(true) {
			/**setting second to count uploads per second**/
			if(start_second == 0)
				start_second = System.nanoTime();
			else if(System.nanoTime() - start_second > 1000000000)
			{
				second_list.add(this_second);
				this_second = 0.0;
				start_second = System.nanoTime();
			}	
			/**if the size of the job is equal to the current upload count, and they're not 0, return**/
			//currently nonfunctional
			if(job_size.peek() != null && job_size.peek() == job_progress)
			{
				job_size.poll(); //removing the size of the completed job from the queue
				job_progress = 0;
				j++;
				
				if(j >= terms.length) {
					thisMetric.add_upload_time(System.nanoTime()-start_time);
					for(double second : second_list)
					{
						per_second += second;
					}
					per_second = per_second/second_list.size();
					thisMetric.add_upload_rate(per_second);
					return;
				}
				currentTerm = terms[j];
			}

			
			/**use some upload method depending on upload mode**/
				switch(upload_mode)
				{
				case 1 :
					while(put_input.peek() != null) {
						if(start_second == 0)
							start_second = System.nanoTime();
						else if(System.nanoTime() - start_second > 1000000000)
						{
							second_list.add(this_second);
							this_second = 0;
							start_second = System.nanoTime();
						}
						try {
							insertTable(db_conn, put_input.poll());
						} catch (SQLException e) {
							e.printStackTrace();
						}
						b++;
					}
				case 2 :
					while(update_input.peek() != null)
					{
						
						//write method to do this new-term check
						if(job_size.peek() != null && job_size.peek() == job_progress)
						{
							job_size.poll(); //removing the size of the completed job from the queue
							job_progress = 0;
							j++;
							
							if(j >= terms.length) {
								thisMetric.add_upload_time(System.nanoTime()-start_time);
								for(double second : second_list)
								{
									per_second += second;
								}
								per_second = per_second/second_list.size();
								thisMetric.add_upload_rate(per_second);
								return;
							}
							currentTerm = terms[j];
						}
						
						if(System.nanoTime() - start_second > 1000000000)
						{
							second_list.add(this_second);
							this_second = 0;
							start_second = System.nanoTime();
						}
						
						b++;
						updateTable(db_conn, update_input.poll());
					}
				case 3 :
					
				case 4 :	
				
				}
		}
	}
	
	
	
	
	
	/*
	 * Open connection to local DB 
	 */
	
	public Connection openConn() throws SQLException
	{
		return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
	}
	
	
	/*
	 * 
	 * Create table to hold course data
	 * 
	 */
	public void createTable(Connection database) throws SQLException
	{
		String table_query_str = "CREATE TABLE " + table + "("
				+ "crndate int,"
				+ "crn int,"
				+ "available int,"
				+ "capacity int,"
				+ "enrolled int,"
				+ "description varchar(500),"
				+ "start_date char(5),"
				+ "end_date char(5),"
				+ "instructor varchar(100),"
				+ "number int,"
				+ "subject varchar(10),"
				+ "term varchar(10),"
				+ "year int,"
				+ "building varchar(10),"
				+ "meet_times varchar(10),"
				+ "credits int,"
				+ "prereqs varchar(500),"
				+ "start_time int,"
				+ "end_time int,"
				+ "restr varchar(100)," 
				+ "extr_chgs varchar(100),"
				+ "PRIMARY KEY(crndate)"
				+ ");";
		
		Statement table_query = database.createStatement();
		table_query.execute(table_query_str);
	}
	
	/*
	 * 
	 * insert item into table
	 * 
	 */
	public void insertTable(Connection database, String to_insert) throws SQLException
	{
		String statement = "INSERT INTO " + table + " " + to_insert;
		Statement table_query = database.createStatement();
		table_query.execute(statement);
	}
	
	public void updateTable(Connection database, String to_insert)
	{
		
	}
	
}
