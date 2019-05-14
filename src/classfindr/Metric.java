package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Object to store clocks and other information
 * 
 * 
 */

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Metric {
	long call_time;
	 long conversion_time;
	 long parse_time;
	 long upload_time;
	 double uploads_per_second;
	 int total_uploads;

	 String table;
	
	 
	/* constructor */
	Metric(String[] terms_in, String table_in)
	{
		table = table_in;
	}
		
	/*********setters*********/
	public synchronized void add_call_time(long time)
	{
		call_time = time;
	}
	
	public synchronized void set_conversion_time(long time)
	{
		conversion_time = time;
	}
	
	public synchronized void set_parse_time(long time)
	{
		parse_time = time;
	}
	
	public synchronized void add_upload_time(long time)
	{
		upload_time = time;
	}
	
	public synchronized void add_upload_rate(double per_second)
	{
		uploads_per_second = per_second;
	}
	
	public synchronized void set_total_uploads(int total)
	{
		total_uploads = total;
	}
	
	
	/* printing to log file */
	public synchronized void log_to_file(String filename) throws IOException
	{
		FileWriter append = new FileWriter(filename, true);
		PrintWriter logs = new PrintWriter(append);
		

			logs.println("----------------------------------------------");
			logs.println("Table: " + table);
			//TODO: Term span here
			logs.println("Server Call time: " + call_time);
			logs.println("Parse Time: " + parse_time + " nsec");
			logs.println("Conversion Time: " + conversion_time + " nsec");
			logs.println("Uploads Queued: " + total_uploads);
			logs.println("Uploads Per Second: " + uploads_per_second);
			logs.println("Upload Time: " + upload_time + " nsec");
			logs.println("----------------------------------------------");

		logs.flush();
		logs.close();
	}
}