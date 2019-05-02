/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Course Class
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.amazonaws.services.dynamodbv2.model.AttributeValue;


public class Course {
	int crn;
	int year;
	String subject;
	int number;
	String description;
	String term = null;
	int capacity;
	int enrolled;
	int available;
	boolean waitlist;
	String instructor;
	String startdate;
	String enddate;
	String attr = null;
	//TODO: Figure out how to dynamically deal with potential multiple meeting times and places both here and in the database
	//I'm kind of thinking maybe for here, a map with char days and a list of ints, which would be start and end times?
	List<String> meettimes = new ArrayList<String>();
	List<Integer> starthrs = new ArrayList<Integer>();
	List<Integer> endhrs = new ArrayList<Integer>();
	//gonna have to be a list of buildings or something, or part of a separate table with crn and all needed meeting information
	String building;
	int credits;
	String extrachgs = null;
	String restrictions = null;
	String prereqs = null;
	
	public String generateValueStr()
	{
		return "VALUES (Course: " + subject + " " + number + ", term: " + term + " " + year + ", capacity: "
	+ capacity + ", enrolled: " + enrolled + ", available: " + available + ", instructor: "	+ instructor + ", CRN: "
	+ crn + ", topic: " + description + ", Start Date: " + startdate + ", End date: " + enddate + ", Attributes: "
	+ attr + ", Building/Room: " + building + ", credits: " + credits + ", Extra Charges: " + extrachgs
	+ ", Prerequisites: " + prereqs + ", Restrictions: " + restrictions	+ ")";
	}
	
	public HashMap<String, AttributeValue> generateItemPush()
	{	
		String primaryKey = "crn";
		String primKeyValue = String.valueOf(crn);
	
		HashMap<String, AttributeValue> toPush = new HashMap<String, AttributeValue>();
			toPush.put(primaryKey, new AttributeValue(primKeyValue));
			noNullPut("year", year, toPush);
			noNullPut("subject", subject, toPush);
			noNullPut("term", term, toPush);
			noNullPut("description", description, toPush);
			noNullPut("capacity", capacity, toPush);
			noNullPut("enrolled", enrolled, toPush);
			noNullPut("available", available, toPush);
			noNullPut("waitlist", waitlist, toPush);
			noNullPut("instructor", instructor, toPush);
			noNullPut("start_date", startdate, toPush);
			noNullPut("end_date", enddate, toPush);
			noNullPut("attributes", attr, toPush);
			noNullPut("building", building, toPush);
			noNullPut("credits", credits, toPush);
			noNullPut("extra_charges", extrachgs, toPush);
			noNullPut("restrictions", restrictions, toPush);
			noNullPut("prerequisites", prereqs, toPush);	
			return toPush;
	}
	
	private void noNullPut(String name, Object input, HashMap<String, AttributeValue> destination)
	{
		if(input != null)
			destination.put(name, new AttributeValue(String.valueOf(input)));
	}
}
