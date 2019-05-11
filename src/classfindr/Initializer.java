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

final class Prefs{
	String table;
	int year_start;
	int year_end;
	int mode;
}

public class Initializer {
	
	/* modes for uploading */
	static final int PUT = 1;
	static final int UPDATE = 2;
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Prefs preferences = new Prefs();
		

		Notifications.setprefs(preferences);
		int current_year = preferences.year_start;

		while(current_year != preferences.year_end)
		{
			/*   */
			String term = String.valueOf(current_year);			
			
			/* initializing shared metric object */
			Metric term_stats = new Metric(term, preferences.table);
			
			/* initializing shared data object */
			final ThreadShare share = new ThreadShare(UPDATE, term, preferences.table, term_stats);
			
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
						
			/* setting the year */
			current_year += 10;
			if(current_year % 100 > 40)
			{
				current_year += 100;
				current_year -= 40;
			}
		}
		Notifications.exit_msg();
	}	
}
