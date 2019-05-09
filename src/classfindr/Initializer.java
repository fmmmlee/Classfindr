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
		/* iterate over years */
		for(int i = 2004; i < 2010; i++)
		{
			/* iterate over trimesters */
			for(int j = 10; j < 50; j+=10)
			{
				term = String.valueOf(i) + String.valueOf(j);
				
				System.out.println("\n\n" + SPACING); //spacing
				
				/* initializing shared metric object */
				Metric term_stats = new Metric(term);
				
				/* initializing shared data object */
				final ThreadShare share = new ThreadShare(UPDATE, term, "course", term_stats);
				
				/* calling WWU servers */
				CallServer call = new CallServer(share);
				Thread call_thread = new Thread(call);
				call_thread.start();
				
				/* waiting for call thread to exit */
				Waiting_Indicators.dots(call_thread, 4);
				System.out.println(SPACING + "              ");
				System.out.println(SPACING + "--- server call complete ---\n");
				
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
		
		System.out.println(SPACING + "\n" + SPACING + "\n" + SPACING + "\n" + SYSMSG +  "------- see metrics.log for program execution stats, exiting now -------");
		System.out.println("\n\n");
		System.out.println("(c) Matthew Lee, 2019");
		System.out.println("MIT license");
		System.out.println("\n");
	}	
}
