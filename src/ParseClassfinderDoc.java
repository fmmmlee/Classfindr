/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Analyzes document returned from CallUniServer
 * 
 */

//TODO: Good error checking analysis throughout whole package


import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseClassfinderDoc{

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
	
	
	//parsing the HTML from CallUniServer into Course objects
    public static int parseDocument(Document unsorted) throws SQLException {
        int err = 0;
        /*
         * 
         * TODO: Database Integration
         * https://sdk.amazonaws.com/java/api/latest/
         * DynamoDbClient or DynamoDbAsyncClient
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
        	
        	System.out.println(columns.get(0).text());
        	
        	//grabs first 4 characters of column, which should be the 4-letter subject tag (e.g. CSCI or HNRS)
        	//Consider making this a switch statement on columns.get(0).text()
        	
        	if(columns.get(0).text().length() > 1)
        	{
        		if(subjects.contains(columns.get(0).text().substring(0, 4)) && temp.subject == null){
        			firstLine(temp, columns);
        		} else if (columns.get(0).text().substring(0, 1) == "/s [MTWRF]") {
        			secondLine(temp, columns);
        		}
        	}
        	
        	
        	System.out.println(temp.generateValueStr());
        	
        	//String sqlQuery = " insert into courses " + temp.generateValueStr();
        	
        }
        
        
        //close sql connection
        //sqlConn.close();
        
    	return err;

    }
    
    
    static void firstLine(Course input, Elements columns)
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
    
    //unfinished
    static void secondLine(Course input, Elements columns)
    {
    	String hold = columns.get(0).text();
    	//TODO: Clean this up, it finds the end of the days-of-the-week section
    	int ender = Math.max(Math.max(Math.max(Math.max(hold.indexOf('F'), hold.indexOf('R')), hold.indexOf('W')), hold.indexOf('T')), hold.indexOf('M'));
    	//adding days to the list of meeting days
    	input.meettimes.add(hold.substring(1, ender));
    	String hours = hold.substring(ender+2, ender+12).replaceAll("!(0-9)", "");
    	if(hold.substring(ender+14, ender+15) == "pm")
    	{
    		input.starthrs.add(12 + Integer.parseInt(hours.substring(0, 3)));
        	input.endhrs.add(12 + Integer.parseInt(hours.substring(4, 7)));
    	} else {
    		input.starthrs.add(12 + Integer.parseInt(hours.substring(0, 3)));
        	input.endhrs.add(12 + Integer.parseInt(hours.substring(4, 7)));
    	}
    	
    	
    	input.building = columns.get(1).text();
    	
    	
    	
    }
    

    public static void main(String[] args) throws IOException, SQLException {

    /*
     Do the below every [time interval] depending on how intensive it is and how often database needs updates
     (if making queries to University database in realtime for tools that already exist, and only doing building and data analytics with this database, latency becomes less of an issue)
     
    	make call
    	
    	parse file
    	
    	feed data into database
    	
    */
    	
    	Document unsorted = CallUniServer.defaultQuery();
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