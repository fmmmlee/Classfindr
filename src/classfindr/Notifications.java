package classfindr;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * If it's printed out and it's not in Waiting_Indicators, it's probably from here
 * I just wanted to aggregate messages printed to the console so that the use of constants and certain expressions would be easier to implement and keep consistent
 */

public class Notifications {
	
	static final String SPACING = "[     ] ";
	static final String ERR = "[" + (char)27 + "[31mERROR" + (char)27 + "[39m] ";
	static final String SYSMSG = "[" + (char)27 + "[34mINFO" + (char)27 + "[39m] ";
	static final String SUCCESS = "[" + (char)27 + "[32mSUCCESS" + (char)27 + "[39m] ";
	static final String STATUS = "[" + (char)27 + "[33mSTATUS" + (char)27 + "[39m]";
	static final String INPUT = "[" + (char)27 + "[33mINPUT" + (char)27 + "[39m]";
	
	/* initial user input */
	static void setprefs(Prefs to_be_set)
	{
		Scanner input = new Scanner(System.in);
		System.out.println(SYSMSG + "Data is available from Fall 2003 to mid-2020.");
		System.out.print(SYSMSG + "Input format: [year term] to [year term] [MODE] [table name]\n" + INPUT + " ");
		String[] prefs = input.nextLine().split(" ");
		List<String> temp_terms = new ArrayList<String>();
		int current_year = 100*(Integer.parseInt(prefs[0]));
		int year_end = 100*(Integer.parseInt(prefs[3]));
		
		switch(prefs[1].toLowerCase())
		{
		case "winter" :
			current_year += 10;
			break;
		case "spring" :
			current_year += 20;
			break;
		case "summer" :
			current_year += 30;
			break;
		case "fall" :
			current_year += 40;
			break;
		}
		
		switch(prefs[4].toLowerCase())
		{
		case "winter" :
			year_end += 10;
			break;
		case "spring" :
			year_end += 20;
			break;
		case "summer" :
			year_end += 30;
			break;
		case "fall" :
			year_end += 40;
			break;
		}
		
		switch(prefs[5])
		{
		case "UPDATE" :
			to_be_set.mode = 2;
			break;
		case "PUT" :
			to_be_set.mode = 1;
			break;
		case "summer" :
			to_be_set.mode = 3;
			break;
		case "fall" :
			to_be_set.mode = 4;
			break;
		}

		
		to_be_set.table = prefs[6];
		
		while(current_year != year_end)
		{
			temp_terms.add(String.valueOf(current_year));
			current_year += 10;
			if(current_year % 100 > 40)
			{
				current_year += 100;
				current_year -= 40;
			}
		}
		
		to_be_set.terms = temp_terms.toArray(new String[temp_terms.size()]);
		input.close();
	}
	
	
	
	
	
	
	/* status Updates */
	static void thread_spun(String name)
	{
		System.out.println(SYSMSG + "### " + name + " thread spun ###");
	}
	
	static void call_initiated()
	{
		System.out.println(SYSMSG + "### calling WWU servers ###");
	}
	
	static void task_finished(String name)
	{
        System.out.println(SUCCESS + "--- " + name + " finished ---                                                              ");
	}
	
	
	
	static void call_backoff(int degree, long wait)
	{
		System.out.println(ERR + "Exponential backoff triggered" + degree + "times");
		System.out.println(ERR + "Next request will be sent in " + wait + ""); //change this to a readable seconds/minutes value
	}
	
	//perhaps print a call time or latency or something (or address)
	static void call_success()
	{
		System.out.println(SUCCESS + "--- server call complete ---                                                             -");
	}
	
	static void exit_msg()
	{
		System.out.println(SYSMSG + "\n"
				+ SYSMSG + "\n"
				+ SYSMSG + "\n"
				+ SUCCESS +  "------- see metrics.log for program execution stats, exiting now -------");
		System.out.println("\n" + SYSMSG + "\n" + SYSMSG);
		System.out.println(SYSMSG + "(c) Matthew Lee, 2019");
		System.out.println(SYSMSG + "MIT license");
		System.out.println(SYSMSG + "\n" + SYSMSG);
	}
	
	/* errors */
	static void bad_response(String term, int responsecode)
	{
		System.out.println(ERR + "bad response. HTTP response code = " + responsecode);
		System.out.println(ERR + "requested term was: " + term);
    	System.out.println(ERR + "see response_log.html for full http of response");
	}
	
}
