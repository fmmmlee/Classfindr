/*
 * 
 * Matthew Lee
 * Spring 2019
 * 
 */

import java.util.ArrayList;
import java.util.List;
public class Course {
	int crn;
	int year;
	String subject;
	int number;
	String description;
	String term = "";
	int capacity;
	int enrolled;
	int available;
	boolean waitlist;
	String instructor;
	String startdate;
	String enddate;
	String attr = "";
	//TODO: Figure out how to dynamically deal with potential multiple meeting times and places both here and in the database
	//I'm kind of thinking maybe for here, a map with char days and a list of ints, which would be start and end times?
	List<String> meettimes = new ArrayList<String>();
	List<Integer> starthrs = new ArrayList<Integer>();
	List<Integer> endhrs = new ArrayList<Integer>();
	String building;
	int credits;
	String extrachgs = "";
	String restrictions = "";
	String prereqs = "";
	
	public String generateValueStr()
	{
		return "VALUES (Course: " + subject + " " + number + ", term: " + term + " " + year + ", capacity: "
	+ capacity + ", enrolled: " + enrolled + ", available: " + available + ", instructor: "	+ instructor + ", CRN: "
	+ crn + ", topic: " + description + ", Start Date: " + startdate + ", End date: " + enddate + ", Attributes: "
	+ attr + ", Building/Room: " + building + ", credits: " + credits + ", Extra Charges: " + extrachgs
	+ ", Prerequisites: " + prereqs + ", Restrictions: " + restrictions	+ ")";
	}
}
