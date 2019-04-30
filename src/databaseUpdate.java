//Matthew Lee
//Spring 2019
//Classfinder Database Scraper
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class databaseUpdate{

	//TODO: Read this from a JSON or YAML which gets updated from the list which can be found by looking at the page source for https://admin.wwu.edu/pls/wwis/wwsktime.SelClass
	private static List<String> subjects = Arrays.asList("A/HI", "ACCT", "AECI", "AHE", "AMST", "ANTH", "ARAB", "ART",
			"ASTR", "AUAP", "BIOL", "BNS", "C/AM", "C2C", "CD", "CHEM", "CHIN", "CISS", "CLST", "COMM",
			"CSCI", "CSD", "CSEC", "DNC", "DSCI", "DSGN", "EAST", "ECE", "ECON", "EDAD", "EDUC", "EE",
			"EGEO", "ELED", "ELL", "ENG", "ENGR", "ENRG", "ENTR", "ENVS", "ESCI", "ESJ", "EUS", "FAIR",
			"FIN", "FREN", "GEOL", "GERM", "GRAD", "GREK", "HGST", "HIST", "HLED", "HNRS", "HRM", "HSP",
			"HUMA", "I T", "IBUS", "ID", "IEP", "INTL", "ITAL", "JAPN", "JOUR", "KIN", "KORE", "LANG",
			"LAT", "LBRL", "LDST", "LIBR", "LING", "M/CS", "MACS", "MATH", "MBA", "MDS", "MFGE", "MGMT",
			"MIS", "MKTG", "MPAC", "MSCI", "MUS", "NURS", "OPS", "PA", "PCE", "PE", "PHIL", "PHYS", "PLSC",
			"PORT", "PSY", "RC", "RECR", "REL", "RUSS", "SALI", "SCED", "SEC", "SMNR", "SOC", "SPAN", "SPED",
			"TESL", "THTR", "VHCL", "WGSS");
	
	//megabytes in terms of bytes
	private static final int MB = 1000000;
	
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

    //jsoup parsing and pushing to database
    public static int parseDocument(Document unsorted) throws SQLException {
        int err = 0;
        
        /*
         * TODO: Database Integration
         * Connection sqlConn = DriverManager.getConnection("", "", "");
         * 
         * 
         *
         */

        //first table is solely column names, second has actual data
        Element table = unsorted.select("table").get(1);
        Elements rows = table.select("tr");
        
        
        //iterating line-by-line
        for(int i = 3; i < rows.size(); i++) 
        {	int courseLine = -1;
        	//skipping empty rows
        	if(rows.get(i) == null)
        		continue;
        	Elements columns = rows.get(i).select("td");
        	Course temp = new Course();
        	
        	//empty rows
        	if(columns.size() < 1)
        		continue;
        	
        	//TODO: put in a while loop here in order to only work on one course until another course is reached, just increment i inside the loop
        	//and throw away the structure after hitting another class and sending the current one to the database
        	
        	//removing empty table columns
        	for(int j = 0; j < columns.size(); j++)
        	{
        		//grabbing crn, which is within a tag
        		if(columns.get(j).select("input").attr("value") != "")
        			temp.crn = Integer.parseInt(columns.get(j).select("input").attr("value"));
        		//removing all blank columns
        		if(!columns.get(j).text().matches(".*[(0-9)(A-Z)(a-z)]+.*"))
        			columns.remove(j);
        	}
        	
        	//empty rows
        	if(columns.size() < 1)
        		continue;
        	
        	for(int j = 0; j < columns.size(); j++)
        	{
        		System.out.println(columns.get(j).text());
        	}
        	
        	//grabs first 4 characters of column, which should be the 4-letter subject tag (e.g. CSCI or HNRS)
        	System.out.println(columns.get(0).text());
        	if(columns.get(0).text().length() > 1 && subjects.contains(columns.get(0).text().substring(0, 4)) && temp.subject == null){
        		courseLine = 0;
        	}
        	
        	switch(courseLine)
        	{
        	case 0 :
        		System.out.println("foo");
        		firstLine(temp, columns);
        		break;
        		
        	case 1 :
        		
        		break;
        		
        	case 2 :
        		
        		break;
        		
        	case 3 :
        		
        		break;
        		
        	case 4 : 
        		
        		break;
        	}
        	
        	
        	System.out.println(temp.generateValueStr());
        	
        	//String sqlQuery = " insert into courses " + temp.generateValueStr();
        	
        }
        
        
        //close sql connection
        //sqlConn.close();
        
    	return err;

    }
    
    
    public static void firstLine(Course input, Elements columns)
    {
    	input.subject = columns.get(0).text().substring(0, 4);
    	//getting the number (i.e. the "101" in "CSCI 101")
    	String hold = columns.get(0).text();
    	input.number = Integer.parseInt(hold.substring(5, hold.length()));
    	//description
    	input.description = columns.get(1).text();
    	//class capacity
    	input.capacity = Integer.parseInt(columns.get(2).text());
    	//enrollment
    	input.enrolled = Integer.parseInt(columns.get(3).text());
    	//available spots
    	input.available = Integer.parseInt(columns.get(4).text());
    	
    	//instructor name
    	input.instructor = columns.get(5).text();
    	
    	//start and end dates - eventually will be replaced with actual calendar/date class
    	String placeholder = columns.get(6).text();
    	input.startdate = placeholder.substring(0, 5);
    	input.enddate = placeholder.substring(6, 11);

    }
    
    
    
    public static String constructQuery(String courseNumber, String term, String gur, String attribute, String location, String subject, String instructor) {
        //term should be in this format (no brackets): [year] [01 through 04] where 01 is winter, 02 spring, 03 summer and 04 fall
        return "sel_subj=dummy&sel_subj=dummy&sel_gur=dummy&sel_gur=dummy&sel_attr=dummy&sel_site=dummy&sel_day=dummy&sel_open=dummy" +
                "&sel_crn=" + courseNumber + "&term=" + term + "&sel_gur=" + gur + "&sel_attr=" + attribute + "&sel_site=" + location + "&sel_subj=" + subject + "&sel_inst=" +
                instructor + "&sel_crse=&begin_hh=0&begin_mi=A&end_hh=0&end_mi=A&sel_cdts=%25";
    }
    
    private static Document defaultQuery() throws IOException
    {
		String currentterm = "201940";
        String queryString = "sel_subj=dummy&sel_subj=dummy&sel_gur=dummy&sel_gur=dummy&sel_attr=dummy&sel_site=dummy&sel_day=dummy&sel_open=dummy&sel_crn=&term="
        + currentterm + "&sel_gur=All&sel_attr=All&sel_site=All&sel_subj=CSCI&sel_inst=ANY&sel_crse=&begin_hh=0&begin_mi=A&end_hh=0&end_mi=A&sel_cdts=%25";
        String theUrl = ("https://admin.wwu.edu/pls/wwis/wwsktime.ListClass");
        org.jsoup.Connection serverCall = Jsoup.connect(theUrl);
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
        serverCall.maxBodySize(10*MB);
        return serverCall.post();
    }
    
    private void testWrite(Document input) throws IOException
    {
    	 File testoutput = new File("testclean.html");

 		FileUtils.writeStringToFile(testoutput, input.outerHtml(), "UTF-8");
    }

    public static void main(String[] args) throws IOException, SQLException {

    /*
     Do the below every [time interval] depending on how intensive it is and how often database needs updates
     (if making queries to University database in realtime for tools that already exist, and only doing building and data analytics with this database, latency becomes less of an issue)
     
    	make call
    	
    	remove HTML tags
    	
    	parse file
    	
    	feed data into database
    	
    */
    	
    	Document unsorted = defaultQuery();
    	parseDocument(unsorted);
    	
        /* Each course also has a little option to view information about it, another POST request with just the CRN and a few hidden inputs. */

        /*
         * 
         * Table (possible garbage rows between):
         * [Beginning of class]
         * empty column
         * Classname
         * Class description
         * (dirty script form entry)
         * some kind of input button, no useful info
         * capacity
         * enrolled
         * available
         * instructor
         * dates
         * 
         * 
         * [second row same class]
         * &nbsp
         * GUR/attributes
         * days and hours
         * Building
         * Credits
         * Extra charges
         * 
         * [possible extra row]
         * &nbsp spanning 2 columns
         * days and hours
         * building
         * 
         */
    }


}