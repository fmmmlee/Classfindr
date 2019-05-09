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


import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.jsoup.nodes.Document;

public class Initializer {
	
	static final int PUT = 1;
	static final int UPDATE = 2;
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		System.out.println("\n\n");
		//accept console input for multiple vs 1 year and multiple vs 1 term(s)
		
		String term = "";
		/* iterate over years */
		for(int i = 2014; i < 2015; i++)
		{
			/* iterate over trimesters */
			for(int j = 10; j < 20; j+=10)
			{
				FileWriter append = new FileWriter("metrics.log", true);
				PrintWriter logs = new PrintWriter(append);
				Metric term_stats = new Metric();
				term = String.valueOf(i) + String.valueOf(j);
				/* calling WWU servers */
				//try to thread this so you can have a ....... (incrementing dots) thing while waiting
				Document unparsed_doc = CallServer.fullTermQuery(term, term_stats);
				
				/* initializing shared thread object */
				final ThreadShare share = new ThreadShare(UPDATE, term, "course", term_stats);
				
				/* initializing class instances */
				ParseDoc parse = new ParseDoc(share, unparsed_doc);
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
				logs.println("----------------------------------------------");
				logs.println("Term: " + term);
				logs.println("Server Call time: " + term_stats.call_time + " nsec");
				logs.println("Parse Time: " + term_stats.parse_time + " nsec");
				logs.println("Conversion Time: " + term_stats.conversion_time + " nsec");
				logs.println("Total Uploads: " + share.size);
				logs.println("Uploads Per Second: " + term_stats.uploads_per_second);
				logs.println("Upload Time: " + term_stats.upload_time + " nsec");
				logs.println("----------------------------------------------");
				logs.flush();
				logs.close();

			}
		}
		
		System.out.println("\n\n\n------- see metrics.log for program execution stats, exiting now -------");
		System.out.println("\n\n");
		System.out.println("(c) Matthew Lee, 2019");
		System.out.println("MIT license");
		System.out.println("\n");
	}	
}
