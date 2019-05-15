package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * 
 */
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



/*
 * Makes call to WWU servers and returns information
 * 
 */
public class ServerCalls implements Runnable{
	
    //Headers for the request to WWU servers
    private static final String HOST = "admin.wwu.edu";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.5";
    private static final String ACCEPT_ENCODING = "gzip, deflate, br";
    private static final String REFERER = "https://admin.wwu.edu/pls/wwis/wwsktime.SelClass";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONNECTION = "keep-alive";
    private static final String COOKIE = "TESTID=SET";
    private static final String INSECURE_REQS = "1";
    static String[] terms;
    static Metric info;
    BlockingQueue<Document> parsed_docs;
    ThreadShare for_document;
    AtomicBoolean finished;
    
    /*
     * 
     * @param	shared	an object of the ThreadShare class that holds information used by this class as well as other classes during concurrent execution.
     */
    ServerCalls(ThreadShare shared)
    {
    	finished = shared.calls_finished;
    	terms = shared.terms;
    	info = shared.metric;
    	parsed_docs = shared.unparsed;
    }
   
    public void run()
    {	//should add documents to the queue as they are received from the server
    	Notifications.call_initiated();
    	for(int i = 0; i < terms.length; i++) {
	    	try {
	    		parsed_docs.put(fullTermQuery(terms[i]));
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	finished.set(true);
    	Notifications.call_success();
    }
    
    //TODO: Make this coherent and function
    //provide full documentation for the available parameters
    static String constructQuery(String courseNumber, String term, String gur, String attribute, String location, String subject, String instructor) {
        //term should be in this format (no brackets): [year] [01 through 04] where 01 is winter, 02 spring, 03 summer and 04 fall
        return "sel_subj=dummy&sel_subj=dummy&sel_gur=dummy&sel_gur=dummy&sel_attr=dummy&sel_site=dummy&sel_day=dummy&sel_open=dummy" +
                "&sel_crn=" + courseNumber + "&term=" + term + "&sel_gur=" + gur + "&sel_attr=" + attribute + "&sel_site=" + location + "&sel_subj=" + subject + "&sel_inst=" +
                instructor + "&sel_crse=&begin_hh=0&begin_mi=A&end_hh=0&end_mi=A&sel_cdts=%25";
    }
    
    
    /*
     * Makes a POST request to the WWU server to retrieve data for a full term of classes.
     * <p>
     * Includes an exponential backoff algorithm with decorrelated jitter. The cap is about 60 seconds.
     * 
     * @param	term	the term to request classes for, in the format [year][term], where [term] is a multiple of 10 from 10 to 40, inclusive
     * @return			a Document object representing the HTML response to the request sent
     */
    static Document fullTermQuery(String term) throws IOException, InterruptedException
    {
    	int sleep_time = 3;
    	Random newRandom = new Random();
    	Document response = null;
	    	while(sleep_time < 60000) {
	    	//TODO: Notification for call for specific term
	    	long start = System.nanoTime();
	        String queryString = "sel_subj=dummy&sel_subj=dummy&sel_gur=dummy&sel_gur=dummy&sel_attr=dummy&sel_site=dummy&sel_day=dummy&sel_open=dummy&sel_crn=&term="
	        + term + "&sel_gur=All&sel_attr=All&sel_site=All&sel_subj=All&sel_inst=ANY&sel_crse=&begin_hh=0&begin_mi=A&end_hh=0&end_mi=A&sel_cdts=%25";
	        String theUrl = ("https://admin.wwu.edu/pls/wwis/wwsktime.ListClass");
	        Connection serverCall = Jsoup.connect(theUrl);
	        
	        serverCall.header("Host", HOST);
	        serverCall.header("User-Agent", USER_AGENT);
	        serverCall.header("Accept", ACCEPT);
	        serverCall.header("Accept-Language", ACCEPT_LANGUAGE);
	        serverCall.header("Accept-Encoding", ACCEPT_ENCODING);
	        serverCall.header("Referer", REFERER);
	        serverCall.header("Content-Type", CONTENT_TYPE);
	        serverCall.header("Content-Length", Integer.toString(queryString.length()));
	        serverCall.header("Connection", CONNECTION);
	        serverCall.header("Cookie", COOKIE);
	        serverCall.header("Upgrade-Insecure-Requests", INSECURE_REQS);
	        serverCall.requestBody(queryString);
	        serverCall.maxBodySize(0);
	        response = serverCall.post();
	        //adding a new call time to the metric object
	        info.add_call_time(System.nanoTime() - start);
	        
	        if(serverCall.response().statusCode() != 200)
	        {
	        	if((sleep_time*=3) < 60000) {
		        	Thread.sleep(newRandom.nextInt(sleep_time));		//max wait 60 seconds
		        	continue;
	        	} else {
	        		Notifications.bad_response(term, serverCall.response().statusCode());
		        	File f = new File("response_log.html");
		        	FileUtils.writeStringToFile(f, response.outerHtml(), "UTF-8");
		        	throw new IOException();
	        	}
	        }
	        if(response.select("table").size() < 2)
	        {
	        	Notifications.bad_response(term, serverCall.response().statusCode());
	        	File f = new File("response_log.html");
	        	FileUtils.writeStringToFile(f, response.outerHtml(), "UTF-8");
	        	throw new IOException();
	        }
	        return response;
    	}
	    return null;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
