package classfindr;
/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Analyzes document returned from CallUniServer
 * 
 */

//TODO: Good error checking analysis throughout whole package



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ParseDoc implements Runnable{

	BlockingQueue<Document> unparsed;
	BlockingQueue<Course> toBeReturned;
	String[] terms;
	BlockingQueue<Integer> sizes;
	AtomicBoolean calls_finished;
	AtomicBoolean finished_parsing;
	Metric thisMetric;
	

	public ParseDoc(ThreadShare shared)
	{
		unparsed = shared.unparsed;
		calls_finished = shared.calls_finished;
		terms = shared.terms;
		toBeReturned = shared.course_queue;
		sizes = shared.size;
		finished_parsing = shared.parse_finished;
		thisMetric = shared.metric;
	}
	
	public void run()
	{
		Notifications.thread_spun("parse");
		//should just keep doing this for each term
		int i = 0;
		while(unparsed.peek() != null || !calls_finished.get()) {
			if(unparsed.peek() != null)
			{
				parseDocument(unparsed.poll(), terms[i]);
				i++;
			}
		}
		finished_parsing.set(true);
		
	}
	
	
	//TODO: swap these lists for a JSON or YAML which gets updated from the list which can be found by looking at the page source for https://admin.wwu.edu/pls/wwis/wwsktime.SelClass
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
	
	private static List<String> attributes = Arrays.asList("ACOM", "ACGM", "BCOM", "BCGM", "CCOM", "HUM", "LSCI",
			"SCI", "QSR", "SSC", "CF", "CF-E", "CPST", "FIG", "FYE", "OL", "SL", "TRVL", "WP1", "WP2", "WP3");
	
	
	//parsing the HTML from CallUniServer into Course objects
	//TODO: This parse is pretty inefficient; it doesn't really matter since I've multithreaded everything and AWS throttling is the rate-limiting step but try to increase 
	//efficiency at some point
    public void parseDocument(Document unsorted, String year_term){
    	
    	/*************CRN DUPLICATE CHECKING*****************/
    	
    	/*Json File for Duplicate Check - this definitely could be pared down*/
    	byte[] encodedFile;
    	String crnstring = "";
    	byte[] encoded_duplicates;
    	String duplicatestring = "";
    	
    	if(!Files.exists(Paths.get("crn.json")))
    	{
    		try {
				Files.createFile(Paths.get("crn.json"));
				FileWriter blankWrite = new FileWriter("crn.json", true);
				PrintWriter blankWriter = new PrintWriter(blankWrite);
				blankWriter.print("[]");
				blankWriter.close();
				blankWrite.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	if(!Files.exists(Paths.get("duplicates.json")))
    	{
    		try {
				Files.createFile(Paths.get("duplicates.json"));
				FileWriter blankWrite = new FileWriter("duplicates.json", true);
				PrintWriter blankWriter = new PrintWriter(blankWrite);
				blankWriter.print("[]");
				blankWriter.close();
				blankWrite.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	try {
			encodedFile = Files.readAllBytes(Paths.get("crn.json"));
			crnstring = new String(encodedFile, "UTF-8");
			encoded_duplicates = Files.readAllBytes(Paths.get("duplicates.json"));
			duplicatestring = new String(encoded_duplicates, "UTF-8");
		} catch (IOException e1) {
			System.out.println("failed to open crn json file");
		}
    	
    	/**creating objects and arrays to write to**/
    	JsonArray duplicates = new JsonParser().parse(duplicatestring).getAsJsonArray();
    	JsonArray oldcrn = new JsonParser().parse(crnstring).getAsJsonArray();
    	
    	/**Writer**/
    	Gson writer = new GsonBuilder().setPrettyPrinting().create();
    	/****************************************************/
    	
    	long start_time = System.nanoTime();
    	
    	int numClasses = 0;
    	//int batchSize = 0;
        /*
         * 
         * TODO: Database Integration
         * https://sdk.amazonaws.com/java/api/latest/
         * DynamoDbClient or DynamoDbAsyncClient
         *
         */
        // year/term setup
        String year = year_term.substring(0, 4);
        String term = "";
    	switch (year_term.substring(4,6))
    	{
    	case "40":
    		term = "fall";
    		break;
    	case "30":
    		term = "summer";
    		break;
    	case "20":
    		term = "spring";
    		break;
    	case "10":
    		term = "winter";
    		break;
    	}
    	
        //first table is solely column names, second has actual data
        Element table = unsorted.select("table").get(1);
        Elements rows = table.select("tr");
        
        //iterating line-by-line
        for(int i = 3; i < rows.size(); i++) 
        {	
        	boolean courseLine = false;
       	
        	Elements columns = rows.get(i).select("td");
        	Course temp = new Course();
        	
        	
        	//CRN
        	temp.courseInfo.put("crn", columns.select("input").attr("value") + " " + year_term);
        	temp.courseInfo.put("year", year);
    		temp.courseInfo.put("term", term);
        	//clean up empty columns and skip row if empty
        	cleanRow(columns);
        	if(columns.size() < 1)
        		continue;        	

        	//can condense this probably
        	if(columns.get(0).text().contains("CLOSED:")){
        		courseLine = true;
        		temp.courseInfo.put("waitlist", "true");
        		columns.get(0).remove();
        		columns = rows.get(i).select("td");
        		cleanRow(columns);
        		mainLine(temp, columns);
        		i++;
        	} else if(subjects.parallelStream().anyMatch(columns.get(0).text() :: contains) && columns.size() == 7 && courseLine == false)
        	{
        		courseLine = true;
        		mainLine(temp, columns);
        		i++;
        	}
        	
        	//possibly change this to a separate method
        	while(courseLine && i < rows.size())
        	{
        		columns = rows.get(i).select("td");
            	cleanRow(columns);
            	if(columns.size() < 1)
            	{
            		i++;
            		continue;
            	}
            	
            	
            	//this clause is because apparently there are a fair few random &amp and &nbsp statements
            	//that the classfinder table generation script drops around in places with actual information
            	
            	for(int k = 0; k < columns.size(); k++)
            	{
            		columns.get(k).text(columns.get(k).text().replaceAll("(\\&).*", ""));
            		if(!containsANumeric(columns.get(k)))
            		{
            				columns.get(k).remove();
            				columns = rows.get(i).select("td");
            				cleanRow(columns);
            		}
            			
            	}
            	
            	//
            	if(columns.size() < 1) {
            		i++;
            		continue;
            	}
            	
            	String checker = columns.get(0).text();            		        		
            	//checking for subjects
        		if(checker.contains("CLOSED:") || subjects.parallelStream().anyMatch(checker :: contains)  && columns.size() == 7){
            		courseLine = false;
            		//decrementing i so that when it increments again on next run of outer for loop mainLine() is triggered
            		i--;
            		break;
            	//or attributes (I could try to merge the conditions with the day/times check but it's a bit tricky so I'm leaving it for now
            	//TODO: this just checks for presence of attribute code and a column size (to avoid random hits from long descriptions)
            	//need to nail down an empirical condition that will work in all situations
            	} else if (attributes.parallelStream().anyMatch(checker :: contains) && columns.size() > 3) {
            		temp.courseInfo.put("attributes", columns.get(0).text());            			
            		timesLine(temp, columns.nextAll());
            	//or restrictions
            	} else if (checker.contains("Restrictions:") && columns.size() > 1) {
            		String curRestr = temp.courseInfo.get("restrictions");
            		temp.courseInfo.put("restrictions", curRestr == null ? columns.get(1).text() : curRestr + " " + columns.get(1).text());
            	//or prereqs
            	} else if (checker.contains("Prerequisites:") && columns.size() > 1) {
            		String curPre = temp.courseInfo.get("prerequisites");
            		temp.courseInfo.put("prerequisites", curPre == null ? columns.get(1).text() : curPre + " " + columns.get(1).text());
            	//or extra meetings
            	} else if (checker.matches("[MTWRF]+(\\s)[0-9]+:.*") || (checker.contains("TBA") && columns.size() > 2)) {
            		timesLine(temp, columns);
            	//for continuations of prereqs or special descriptions
            	} else {
            		String curDes = temp.courseInfo.get("description");
            		temp.courseInfo.put("description", curDes == null ? checker : curDes + checker);
            	}
            	i++;
            }
        	
        	/*****Condition Checking for Duplicate CRNs*****/
        	String crn = temp.courseInfo.get("crn");
        	JsonElement crnEle = new JsonPrimitive(crn);
        	if(oldcrn.contains(crnEle))
        	{
        		duplicates.add(new JsonPrimitive(temp.printInfo()));
        	} else {
        		oldcrn.add(crnEle);
        	}
    		/************************************************/
        	
        	
        	/**Pushing Course into queue**/
        	try {
				toBeReturned.put(temp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        	/*incrementing number of courses*/
        	numClasses++;
        }
        

        /****Duplicate Checking File Updates*****/
        try {
        	FileWriter crn_write = new FileWriter("crn.json");
        	FileWriter dup_write = new FileWriter("duplicates.json");
        	writer.toJson(oldcrn, crn_write);
			writer.toJson(duplicates, dup_write);
        	crn_write.flush();
        	dup_write.flush();
        	crn_write.close();
        	dup_write.close();
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /********End Duplicate Checking*********/
        
        
        /***time elapsed***/
        thisMetric.set_parse_time(System.nanoTime()-start_time);
        
        
        Notifications.task_finished("parse of " + term + " " + year);
        try {
			sizes.put(numClasses);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    
    static void mainLine(Course input, Elements columns)
    {    	
    	//description
    	input.courseInfo.put("description", columns.get(1).text());
    	
    	//course subject and number
    	String hold = columns.get(0).text();
    	String probSubj = hold.replaceAll("[^(A-Z)]+", "");
    	if(probSubj.length() > 4)
    	{
    		input.courseInfo.put("subject", probSubj.substring(0, 4));
    		String curDes = input.courseInfo.get("description");
    		String note = "Note: The class number of this class also has \'" + probSubj.substring(4, probSubj.length()) + "\' appended to it";
    		input.courseInfo.put("description", curDes == null ? note : curDes + " |" + note);
    	} else {
    		input.courseInfo.put("subject", probSubj);
    	}
    	input.courseInfo.put("number", hold.replaceAll("\\D+",""));
    	//class capacity
    	input.courseInfo.put("capacity", columns.get(2).text());
    	//enrollments
    	input.courseInfo.put("enrolled", columns.get(3).text());
    	//available spots
    	input.courseInfo.put("available", columns.get(4).text());
    	
    	//instructor name
    	input.courseInfo.put("instructor", columns.get(5).text());
    	//start and end dates - eventually will be replaced with actual calendar/date class
    	String placeholder = columns.get(6).text();
    	if(placeholder.contains("TBA"))
    	{
    		String curDes = input.courseInfo.get("description");
    		String note = "Note: Dates for this class have not yet been decided";
    		input.courseInfo.put("description", curDes == null ? note : curDes + " |" + note);
    		return;
    	}
    	
    	input.courseInfo.put("start_date", placeholder.substring(0, 5));
    	input.courseInfo.put("end_date", placeholder.substring(6, 11));
    }
    

    static void timesLine(Course input, Elements columns)
    {
    	//meeting days and hours
    	String hold = columns.get(0).text();
    	
    	String hours = hold.replaceAll("[^\\d+]", "");
    	String meetTime = hold.replaceAll("[^(MTWRF)]+", "");
    	String stTime = input.courseInfo.get("start_time");
    	String enTime = input.courseInfo.get("end_time");
    	
    	if(hold.contains("TBA"))
    	{
    		input.courseInfo.put("description", input.courseInfo.get("description") + "|Note: No class/lab times at time of last update");
    	} else if (!hold.matches("[MTWRF]+(\\s)[0-9]+:.*")) {
    		input.courseInfo.put("description", input.courseInfo.get("description") + "|Nonstandard class time: " + hold);
    		//dealing with am/pm
    	} else if (hold.contains("am")) {
    		input.courseInfo.put("start_time", stTime == null ? hours.substring(0, 4) : stTime + " " + hours.substring(0, 4));
        	input.courseInfo.put("end_time", enTime == null ? hours.substring(4) : enTime + " " + hours.substring(4));
    	} else {
    		//the extra if-else statements here are for times in the 12 pm block
    		if(Integer.parseInt(hours.substring(0, 4)) >= 1200)
    			input.courseInfo.put("start_time", stTime == null ? hours.substring(0, 4) : stTime + " " + hours.substring(0, 4));
    		else
    			input.courseInfo.put("start_time", stTime == null ? String.valueOf(1200 + Integer.parseInt(hours.substring(0, 4))) : stTime + " " + String.valueOf(1200 + Integer.parseInt(hours.substring(0, 4))));
    		
    		if(Integer.parseInt(hours.substring(4)) >= 1200)
    			input.courseInfo.put("end_time", enTime == null ? hours.substring(4) : enTime + " " + hours.substring(4));
    		else
    			input.courseInfo.put("end_time", stTime == null ? String.valueOf(1200 + Integer.parseInt(hours.substring(4))) : enTime + " " + String.valueOf(1200 + Integer.parseInt(hours.substring(4))));
    	}
    	
    	String meet = input.courseInfo.get("meet_times");
    	if(meetTime != null && meetTime != "");
    		input.courseInfo.put("meet_times", meet == null ? hold.replaceAll("[^(MTWRFS)]+", "") : meet + " " + hold.replaceAll("[^(MTWRFS)]+", ""));
    	
    	String curBuild = input.courseInfo.get("building");
    	input.courseInfo.put("building", curBuild == null ? columns.get(1).text() : input.courseInfo.get("building") + "  " + columns.get(1).text());
    	
    	//extra things that might not occur on all lines
    	// -credits will be in every course but will not appear on a line for extra meetings
    	// -extra charges may or may not be in a course
    	if(columns.size() >= 3) {
    		if(columns.get(2).text().contains("-"))
    			input.courseInfo.put("description", input.courseInfo.get("description") + " |Note: Credit value is variable, ranging from " + columns.get(2).text());
    		else
    			input.courseInfo.put("credits", columns.get(2).text().replaceAll("[^\\d+]", ""));
    	}
    	
    	if (columns.size() == 4)
    		input.courseInfo.put("extra_charges", columns.get(3).text());	
    }

    
    //utility to remove columns consisting solely of whitespace/newlines
    //could use just hasText() on the row itself, must check to see if it works here
    static Elements cleanRow(Elements input) {
    	for(int j = 0; j < input.size(); j++)
    	{
    		if(!input.hasText() || !containsANumeric(input.get(j)) || input.get(j).attr("class") == "ddheader")
    		{
    			input.remove(j);
    			j--;
    		}
    	}
    	return input;
    }
    
    //utility to check if an element has anything alphanumeric
    static boolean containsANumeric(Element input)
    {
    	if(input.hasText() && input.text().matches(".*[(0-9)(A-Z)(a-z)]+.*") && input.text().replaceAll("\\&.*;.*", "").length() > 0){
    		return true;
    	}
    	return false;
    }
}