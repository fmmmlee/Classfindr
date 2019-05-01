/*
 * 
 * Matthew Lee
 * Spring 2019
 * 
 */

import java.util.List;
public class Course {
	int crn;
	int year;
	String subject;
	int number;
	String description;
	String term;
	int capacity;
	int enrolled;
	int available;
	String instructor;
	String startdate;
	String enddate;
	String attr;
	//TODO: Figure out how to dynamically deal with potential multiple meeting times and places both here and in the database
	//I'm kind of thinking maybe for here, a map with char days and a list of ints, which would be start and end times?
	List<String> meettimes;
	List<Integer> starthrs;
	List<Integer> endhrs;
	String building;
	int credits;
	String extrachgs;
	
	public String generateValueStr()
	{
		return "VALUES ("
	+ subject + ", "
	+ number + ", "
	+ year + ", "
	+ term + ", "
	+ capacity + ", " 
	+ enrolled + ", "
	+ available + ", "
	+ instructor + ", "
	+ startdate + ", "
	+ enddate + ", "
	+ attr + ", "
	+ meettimes + ", "
	+ building + ", "
	+ credits + ", "
	+ extrachgs
	+ ")";
	}
}
