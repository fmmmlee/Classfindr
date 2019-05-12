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

final class Prefs
{
	String table;
	String[] terms = null;
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
			
			/* initializing shared data object */
			final ThreadShare share = new ThreadShare(UPDATE, preferences.terms, preferences.table);
			
			/* calling WWU servers */
			CallServer call = new CallServer(share);
			Thread call_thread = new Thread(call);
			call_thread.start();
			
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
			share.metric.log_to_file("metrics.log");
						

		Notifications.exit_msg();
	}
}
