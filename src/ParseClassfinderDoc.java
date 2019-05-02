/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Analyzes document returned from CallUniServer
 * 
 */

//TODO: Good error checking analysis throughout whole package


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseClassfinderDoc{

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
    public static int parseDocument(Document unsorted, String currentTerm){
    	File fancyTester = new File("fancyTester.txt");
    	PrintWriter fancyWrite = null;
		try {
			fancyWrite = new PrintWriter("fancyTester.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int numClasses = 0;
        int err = 0;
        /*
         * 
         * TODO: Database Integration
         * https://sdk.amazonaws.com/java/api/latest/
         * DynamoDbClient or DynamoDbAsyncClient
         *
         */
        // year/term setup
        int year = Integer.parseInt(currentTerm.substring(0, 4));
        String term = "";
    	switch (currentTerm.substring(4,6))
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
        	
        	//year
        	temp.year = year;
        	//term
           	temp.term = term;
        	
        	//CRN
        	temp.crn = Integer.parseInt(columns.select("input").attr("value"));
        	
        	//clean up empty columns and skip row if empty
        	cleanRow(columns);
        	if(columns.size() < 1)
        		continue;        	

        	//can condense this probably
        	if(columns.get(0).text().contains("CLOSED:")){
        		courseLine = true;
        		temp.waitlist = true;
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
            		temp.attr = columns.get(0).text();            			
            		timesLine(temp, columns.nextAll());
            	//or restrictions
            	} else if (checker.contains("Restrictions:")) {
            			temp.restrictions += columns.get(1).text();
            	//or prereqs
            	} else if (checker.contains("Prerequisites:")) {
            			temp.prereqs += columns.get(1).text();
            	//or extra meetings
            	} else if (checker.matches("[MTWRF]+(\\s)[0-9]+:.*") || (checker.contains("TBA") && columns.size() > 2)) {
            		timesLine(temp, columns);
            	//for continuations of prereqs or special descriptions
            	} else {
            		temp.description += " " + checker;
            	}

            	i++;
            }
            	
            	System.out.println(temp.generateValueStr());	
            	fancyWrite.println(temp.generateValueStr());
            	numClasses++;
        }
        fancyWrite.close();
        System.out.println(numClasses);	     	
        return err;
    }

    
    
    static void mainLine(Course input, Elements columns)
    {    	
    	//description
    	input.description = columns.get(1).text();
    	
    	//course subject and number
    	String hold = columns.get(0).text();
    	String probSubj = hold.replaceAll("[^(A-Z)]+", "");
    	if(probSubj.length() > 4)
    	{
    		input.subject = probSubj.substring(0, 4);
    		input.description += " |Note: The class number of this class also has \'" + probSubj.substring(4, probSubj.length()) + "\' appended to it";
    	} else {
    		input.subject = probSubj;
    	}
    	input.number = Integer.parseInt(hold.replaceAll("\\D+",""));
    	//class capacity
    	input.capacity = Integer.parseInt(columns.get(2).text());
    	//enrollments
    	input.enrolled = Integer.parseInt(columns.get(3).text());
    	//available spots
    	input.available = Integer.parseInt(columns.get(4).text());
    	
    	//instructor name
    	input.instructor = columns.get(5).text();
    	
    	//start and end dates - eventually will be replaced with actual calendar/date class
    	String placeholder = columns.get(6).text();
    	if(placeholder.contains("TBA"))
    	{
    		input.description += "|Note: Dates for this class have not yet been decided";
    		return;
    	}
    	input.startdate = placeholder.substring(0, 5);
    	input.enddate = placeholder.substring(6, 11);
    }
    

    static void timesLine(Course input, Elements columns)
    {
    	//meeting days and hours
    	String hold = columns.get(0).text();
    	
    	String hours = hold.replaceAll("[^\\d+]", "");
    	if(hold.contains("TBA"))
    	{
    		input.description += "|Note: No class times at time of last update";
    	} else if (!hold.matches("[MTWRF]+(\\s)[0-9]+:.*")) {
    		input.description += "|Note: Nonstandard class time: " + hold;
    		//dealing with am/pm
    	} else if (hold.contains("am")) {
    		input.starthrs.add(Integer.parseInt(hours.substring(0, 3)));
        	input.endhrs.add(Integer.parseInt(hours.substring(4, 7)));
    	} else {
    		//start time
    		if(Integer.parseInt(hours.substring(0, 3)) > 900)
    				input.starthrs.add(Integer.parseInt(hours.substring(0, 3)));
    		else
    			input.starthrs.add(1200 + Integer.parseInt(hours.substring(0, 3)));
    		//end time
    		if(Integer.parseInt(hours.substring(4, 7)) > 1100)
    			input.endhrs.add(Integer.parseInt(hours.substring(4, 7)));
    		else
    			input.endhrs.add(1200 + Integer.parseInt(hours.substring(4, 7)));
    	}
    	
    	input.meettimes.add(hold.replaceAll("[^(MTWRF)]+", ""));
    	
    	input.building = columns.get(1).text();
    	//extra things that might not occur on all lines
    	// -credits will be in every course but will not appear on a line for extra meetings
    	// -extra charges may or may not be in a course
    	if(columns.size() >= 3) {
    		if(columns.get(2).text().contains("-"))
    			input.description += "|Note: Credit value is variable, ranging from " + columns.get(2).text();
    		else {
    			input.credits = Integer.parseInt(columns.get(2).text().replaceAll("[^\\d+]", ""));
    			}
    	}
    	
    	if (columns.size() == 4)
    		input.extrachgs += columns.get(3).text();	
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