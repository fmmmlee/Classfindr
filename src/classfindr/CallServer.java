package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Makes call to WWU servers and returns information
 * 
 */
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpResponseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

//TODO: Multithread this and grab like all historical docs 2003-2018 concurrently
//TODO: Implement exponential backoff algorithm and timeout to avoid committing seppuku during outages

public class CallServer implements Runnable{
	
	//
	static final String SPACING = "[     ] ";
	static final String ERR = "[ERROR] ";
	static final String SYSMSG = "[INFO-] ";
	
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
    static String term;
    static Metric info;
    Document parsed;
    ThreadShare for_document;
    
    CallServer(ThreadShare shared)
    {
    	for_document = shared;
    	term = shared.term;
    	info = shared.metric;
    	parsed = shared.unparsed;
    }
   
    public void run()
    {
    	try {
			for_document.set_document(fullTermQuery());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //TODO: Make this coherent and function
    //provide full documentation for the available parameters
    static String constructQuery(String courseNumber, String term, String gur, String attribute, String location, String subject, String instructor) {
        //term should be in this format (no brackets): [year] [01 through 04] where 01 is winter, 02 spring, 03 summer and 04 fall
        return "sel_subj=dummy&sel_subj=dummy&sel_gur=dummy&sel_gur=dummy&sel_attr=dummy&sel_site=dummy&sel_day=dummy&sel_open=dummy" +
                "&sel_crn=" + courseNumber + "&term=" + term + "&sel_gur=" + gur + "&sel_attr=" + attribute + "&sel_site=" + location + "&sel_subj=" + subject + "&sel_inst=" +
                instructor + "&sel_crse=&begin_hh=0&begin_mi=A&end_hh=0&end_mi=A&sel_cdts=%25";
    }
    
    static Document fullTermQuery() throws IOException
    {
    	System.out.println(SYSMSG + "### calling WWU servers ###\n\n");
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
        Document response = serverCall.post();
        info.set_call_time(System.nanoTime() - start);
        if(response.select("table").size() < 2)
        {
        	
        	System.out.println(ERR + "bad response. requested term was: " + term);
        	System.out.println(ERR + "see response_log.html for full http of response");
        	File f = new File("response_log.html");
        	FileUtils.writeStringToFile(f, response.outerHtml(), "UTF-8");
        	throw new IOException();
        	
        }
        return response;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
