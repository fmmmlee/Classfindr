package classfindr;

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
	
	
	/* initial user input */
	static void setprefs(Prefs empty)
	{
		Scanner input = new Scanner(System.in);
		System.out.println(SYSMSG + "Data is available from Fall 2003 to mid-2020.");
		System.out.println(SYSMSG + "Input format: [year term] to [year term] [MODE] [table name]");
		String[] prefs = input.nextLine().split(" ");
		empty.year_start = 100*(Integer.parseInt(prefs[0]));
		empty.year_end = 100*(Integer.parseInt(prefs[3]));
		
		switch(prefs[1].toLowerCase())
		{
		case "winter" :
			empty.year_start += 10;
			break;
		case "spring" :
			empty.year_start += 20;
			break;
		case "summer" :
			empty.year_start += 30;
			break;
		case "fall" :
			empty.year_start += 40;
			break;
		}
		
		switch(prefs[4].toLowerCase())
		{
		case "winter" :
			empty.year_end += 10;
			break;
		case "spring" :
			empty.year_end += 20;
			break;
		case "summer" :
			empty.year_end += 30;
			break;
		case "fall" :
			empty.year_end += 40;
			break;
		}
		
		switch(prefs[5])
		{
		case "UPDATE" :
			empty.mode = 2;
			break;
		case "PUT" :
			empty.mode = 1;
			break;
		case "summer" :
			empty.mode = 3;
			break;
		case "fall" :
			empty.mode = 4;
			break;
		}

		empty.table = prefs[6];
		
		input.close();
	}
	
	
	
	
	
	
	/* status Updates */
	static void thread_spun(String name)
	{
		System.out.println(SYSMSG + "### " + name + " thread spun ###");
	}
	
	static void call_initiated()
	{
		System.out.println(SYSMSG + "### calling WWU servers ###" + "\n" + SPACING + "\n" + SPACING);
	}
	
	static void task_finished(String name)
	{
        System.out.println(SPACING + "\n" + SUCCESS + "--- " + name + " finished ---");
	}
	
	
	
	static void call_backoff(int degree, long wait)
	{
		System.out.println(ERR + "Exponential backoff triggered" + degree + "times");
		System.out.println(ERR + "Next request will be sent in " + wait + ""); //change this to a readable seconds/minutes value
	}
	
	//perhaps print a call time or latency or something (or address)
	static void call_success()
	{
		System.out.println(SPACING + "              ");
		System.out.println(SPACING + "--- server call complete ---" + "\n" + SPACING);
	}
	
	static void exit_msg()
	{
		System.out.println(SPACING + "\n"
				+ SPACING + "\n"
				+ SPACING + "\n"
				+ SYSMSG +  "------- see metrics.log for program execution stats, exiting now -------");
		System.out.println("\n" + SPACING + "\n" + SPACING);
		System.out.println("(c) Matthew Lee, 2019");
		System.out.println(SPACING + "MIT license");
		System.out.println(SPACING + "\n" + SPACING);
	}
	
	/* errors */
	static void bad_response(String term)
	{
		System.out.println(ERR + "bad response. requested term was: " + term);
    	System.out.println(ERR + "see response_log.html for full http of response");
	}
	
}
