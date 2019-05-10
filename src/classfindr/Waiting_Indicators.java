package classfindr;

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

	static final String SPACING = "[     ] ";
	static final String ERR = "[ERROR] ";
	static final String SYSMSG = "[INFO] ";
	
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
	public static void progress_bar(int length, int progress, int goal, boolean print_header)
	{
		if(print_header) {
			System.out.println("\n\n\n       -~-~-~-~-~ Amazon DynamoDB Upload Progress ~-~-~-~-~-       \n");
    	}
		
		if(progress<goal)
			System.out.print("   ["
					+ StringUtils.repeat('|',(int) (length*((double) progress/((double) goal))))
					+ StringUtils.repeat(' ', (int) (length*(1.0-((double) progress/((double) goal))))) + "]"
					+ StringUtils.repeat(" ", 6 - (int) Math.log10((double) progress)) + progress + "/" + goal + "   \r");
		else {
			System.out.println("   ["
					+ StringUtils.repeat('|',(int) (length*((double) progress/((double) goal))))
					+ StringUtils.repeat(' ', (int) (length*(1.0-((double) progress/((double) goal))))) + "]"
					+ StringUtils.repeat(" ", 6 - (int) Math.log10((double) progress)) + progress + "/" + goal + "\n\n\n" + "                  -~-~-~ Upload Complete ~-~-~-\n");
		}
	}
	
	
}
