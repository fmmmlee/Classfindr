package classfindr.ConsoleInterface;

import static classfindr.Utility.Constants.*;

/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * If it's printed out and it's not in Waiting_Indicators, it's probably from here
 * I just wanted to aggregate messages printed to the console so that the use of constants and certain expressions would be easier to implement and keep consistent
 */

public class Notifications {
	
	/* status Updates */
	public static void thread_spun(String name)
	{
		System.out.println(SYSMSG + "### " + name + " thread spun ###");
	}
	
	public static void call_initiated()
	{
		System.out.println(SYSMSG + "### calling WWU servers ###");
	}
	
	public static void task_finished(String name)
	{
        System.out.println(SUCCESS + "--- " + name + " finished ---                                                              ");
	}
	
	
	
	public static void call_backoff(int degree, long wait)
	{
		System.out.println(ERR + "Exponential backoff triggered" + degree + "times");
		System.out.println(ERR + "Next request will be sent in " + wait + ""); //change this to a readable seconds/minutes value
	}
	
	//perhaps print a call time or latency or something (or address)
	public static void call_success()
	{
		System.out.println(SUCCESS + "--- server call complete ---                                                             -");
	}
	
	public static void exit_msg()
	{
		System.out.println(SYSMSG + "\n"
				+ SYSMSG + "\n"
				+ SYSMSG + "\n"
				+ SUCCESS +  "------- see metrics.log for program execution stats, exiting now -------");
		System.out.println(SYSMSG + "\n" + SYSMSG);
		System.out.println(SYSMSG + "(c) Matthew Lee, 2019");
		System.out.println(SYSMSG + "MIT license");
		System.out.println(SYSMSG + "\n" + SYSMSG);
	}
	
	/* errors */
	public static void bad_response(String term, int responsecode)
	{
		System.out.println(ERR + "bad response. HTTP response code = " + responsecode);
		System.out.println(ERR + "requested term was: " + term);
    	System.out.println(ERR + "see response_log.html for full http of response");
	}
	
}
