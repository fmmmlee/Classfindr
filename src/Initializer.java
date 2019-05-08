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

import org.jsoup.nodes.Document;



public class Initializer {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		int mode = 2;
		String term = "201930";
		
		/* calling WWU servers */
		Document unparsed_doc = CallServer.fullTermQuery(term);
		
		/* initializing shared thread object */
		final ThreadShare share = new ThreadShare(mode, term, "course");
		
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
		System.out.println("parse finished");
		converter_thread.get();
		System.out.println("conversion finished");
		upload_thread.get();
		System.out.println("upload finished");
		
		
	}
	
}
