package classfindr.Utility;

/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Various types of waiting indicators
 * 
 * Stuff like progress bars, spinny dots, that sort of thing
 */

import org.apache.commons.lang3.StringUtils;


//TODO: make the progress bar portable and put it in this class
public class Waiting_Indicators {

	static final String ERR = "[" + (char)27 + "[31mERROR" + (char)27 + "[39m] ";
	static final String SYSMSG = "[" + (char)27 + "[34mINFO" + (char)27 + "[39m] ";
	static final String SUCCESS = "[" + (char)27 + "[32mSUCCESS" + (char)27 + "[39m] ";
	static final String STATUS = "[" + (char)27 + "[33mSTATUS" + (char)27 + "[39m]";
	static final String SPACING = "[     ] ";
	
	/* prints out dots in sequence */
	public static void dots(Thread waiting_on, int num_dots)
	{
		long start_time = System.nanoTime();
		while(waiting_on.isAlive())
		{
			long sec = (System.nanoTime() - start_time)/1000000000;
			System.out.print(SPACING + StringUtils.repeat(".", (int) sec) + "         \r");
			if(sec > num_dots)
			{
				start_time = System.nanoTime();
			}
		}
	}
	
	/* simple console progress bar */
	//TODO: in the mid-600s / 2576 there was a hop at the end of the line; I assume it's an idiosyncrasy of the casts between double and int here
	//also maybe change the header to be a parameter
	public static void progress_bar(int length, int progress, int goal, boolean print_header, String header, String label)
	{
		if(print_header) {
			System.out.println(STATUS + " " + header);
    	}
		
		if(progress<goal)
			System.out.print(STATUS + " " + label + "   ["
					+ StringUtils.repeat('=',(int) (length*((double) progress/((double) goal)))) + ">"
					+ StringUtils.repeat(' ', (int) (length*(1.0-((double) progress/((double) goal))))) + "]"
					+ StringUtils.repeat(" ", 6 - (int) Math.log10((double) progress)) + progress + "/" + goal + "   \r");
		else {
			System.out.println(SUCCESS + " "  + label + "   ["
					+ StringUtils.repeat('=',(int) (length*((double) progress/((double) goal))))
					+ StringUtils.repeat(' ', (int) (length*(1.0-((double) progress/((double) goal))))) + "]"
					+ StringUtils.repeat(" ", 6 - (int) Math.log10((double) progress)) + progress + "/" + goal);
		}
	}
	
	
}
