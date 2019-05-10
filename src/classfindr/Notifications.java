package classfindr;

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
	static final String ERR = "[ERROR] ";
	static final String SYSMSG = "[INFO] ";
	static final String SUCCESS = "[SUCCESS] ";
	
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
