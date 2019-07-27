package classfindr.Utility;

/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Constants used throughout application
 * 
 * 
 */

public class Constants {
	
	/* database types */
	public static final int LOCAL = 0;		//local and embedded interchangeable
	public static final int EMBEDDED = 0;
	public static final int AWS = 1;
	
	/* database write types */
	public static final int INSERT = 1;
	public static final int UPDATE = 2;
	
	/* console output type indicators */
	public static final String SPACING = "[     ] ";
	public static final String ERR = "[" + (char)27 + "[31mERROR" + (char)27 + "[39m] ";
	public static final String SYSMSG = "[" + (char)27 + "[34mINFO" + (char)27 + "[39m] ";
	public static final String SUCCESS = "[" + (char)27 + "[32mSUCCESS" + (char)27 + "[39m] ";
	public static final String STATUS = "[" + (char)27 + "[33mSTATUS" + (char)27 + "[39m]";
	public static final String INPUT = "[" + (char)27 + "[33mINPUT" + (char)27 + "[39m]";
	
}
