package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Spawn Instances
 * 
 * 
 * 
 */

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Initializer {
	
	/* modes for uploading */
	static final int PUT = 1;
	static final int UPDATE = 2;
	
	/* padding */
	static final String SPACING = "[     ] ";
	static final String ERR = "[ERROR] ";
	static final String SYSMSG = "[INFO-] ";
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		
		//accept console input for multiple vs 1 year and multiple vs 1 term(s)
		
		String term = "";
		String table = "courses";
		/* iterate over years */
		for(int i = 2006; i < 2010; i++) //should be missing 2005 summer/fall and 2006 spring-fall
		{
			/* iterate over trimesters */
			for(int j = 10; j < 50; j+=10)
			{
				term = String.valueOf(i) + String.valueOf(j);
				
				System.out.println("\n\n" + SPACING); //spacing
				
				/* initializing shared metric object */
				Metric term_stats = new Metric(term, table);
				
				/* initializing shared data object */
				final ThreadShare share = new ThreadShare(UPDATE, term, table, term_stats);
				
				/* calling WWU servers */
				CallServer call = new CallServer(share);
				Thread call_thread = new Thread(call);
				call_thread.start();
				
				/* waiting for call thread to exit */
				Waiting_Indicators.dots(call_thread, 4);
				Notifications.call_success();
				
				/* initializing class instances */
				ParseDoc parse = new ParseDoc(share);
				UploadToAWS upload = new UploadToAWS(share);
				CourseConvert converter = new CourseConvert(share);
				
				/* spinning threads */
				CompletableFuture<Void> parse_thread = CompletableFuture.runAsync(parse);
				CompletableFuture<Void> upload_thread = CompletableFuture.runAsync(upload);
				CompletableFuture<Void> converter_thread = CompletableFuture.runAsync(converter);
				
				/* watching output */
				parse_thread.get();
				converter_thread.get();
				upload_thread.get();
				
				/* printing metrics to log */
				term_stats.log_to_file("metrics.log");

			}
		}
		Notifications.exit_msg();
	}	
}
