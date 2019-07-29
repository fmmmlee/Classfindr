package classfindr;

/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Spawns instances of threads and executes.
 * 
 * 
 * 
 */

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import classfindr.ConsoleInterface.Interface;
import classfindr.ConsoleInterface.Notifications;
import classfindr.Threads.CourseConverter;
import classfindr.Threads.ParseDoc;
import classfindr.Threads.ServerCalls;
import classfindr.Threads.DatabaseAccess.AccessLocalDB;
import classfindr.Threads.DatabaseAccess.UploadToAWS;
import classfindr.Threads.SharedData.Preferences;
import classfindr.Threads.SharedData.RuntimeConfig;

import static classfindr.Utility.Constants.*;

//TODO: If one thread throws an exception, cease execution on the others and give an error message and exit properly
//TODO: improve names (particularly ThreadShare/share/shared etc.)
public class Initializer {
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Preferences preferences = new Preferences();
		
		/* accepting user input */
		Interface.setprefs(preferences);
		
		/* initializing shared data object */
		final RuntimeConfig share = new RuntimeConfig(preferences);
		
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
