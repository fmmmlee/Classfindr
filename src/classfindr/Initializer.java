package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Spawns instances of threads and executes.
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
	int database;
}

//TODO: If one thread throws an exeption, cease execution on the others and give an error message and exit properly
public class Initializer {
	
	/* modes for uploading */
	static final int INSERT = 1;
	static final int UPDATE = 2;
	
	/* database destinations */
	static final int LOCAL = 0;
	static final int AWS = 1;
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Prefs preferences = new Prefs();
		
		/* accepting user input */
		Notifications.setprefs(preferences);
		
		/* initializing shared data object */
		final ThreadShare share = new ThreadShare(preferences.database, preferences.mode, preferences.terms, preferences.table); //TODO: Have ThreadShare just accept a Prefs object
		
		/* calling WWU servers on new thread */
		ServerCalls call = new ServerCalls(share);
		Thread call_thread = new Thread(call);
		call_thread.start();
		
		/* initializing class instances */
		ParseDoc parse = new ParseDoc(share);
		UploadToAWS upload = new UploadToAWS(share);
		AccessLocalDB localDB = new AccessLocalDB(share);
		CourseConverter converter = new CourseConverter(share);
		
		/* spinning threads */
		CompletableFuture<Void> parse_thread = CompletableFuture.runAsync(parse);
		CompletableFuture<Void> upload_thread = (preferences.database == AWS ? CompletableFuture.runAsync(upload) : CompletableFuture.runAsync(localDB));
		CompletableFuture<Void> converter_thread = CompletableFuture.runAsync(converter);
		
		/* waiting for output */
		parse_thread.get();
		converter_thread.get();
		upload_thread.get();
		
		/* printing metrics to log */
		share.metric.log_to_file("metrics.log");
				
		/* console exit message */
		Notifications.exit_msg();
	}
}
