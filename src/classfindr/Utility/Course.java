package classfindr.Utility;
/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Course Class
 * 
 */


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;

//TODO: Set up batch insert/update - see H2 documentation
public class Course{
	
	/** holds all the information about a course */
	public final HashMap<String, String> courseInfo = new HashMap<String, String>();

	
	public Course(){}	//default constructor	
	
	
	/******* FOR LOCAL DATABASE *******/
	
	
	
	
	/**
	 * Generate a SQL query to insert a tuple representing this course into a table
	 * 
	 * @param table_name the table to be updated
	 * @return
	 */
	public String generateLocalInsert(String table_name)
	{
		String columns = "(";
		String values = "VALUES(";
		/* adding keys and values to column and value portions of query respectively */
		for(Entry<String, String> course : courseInfo.entrySet())
		{
			columns += course.getKey() + ",";
			values += "\'" + course.getValue().replaceAll("[']", "") + "\'" + ",";
		}
		
		columns = columns.substring(0, columns.length()-1) + ")";		//replacing trailing ',' with a close parenthesis
		values = values.substring(0, values.length()-1) + ")";			//replacing trailing ',' with a close parenthesis
		
		return "INSERT INTO " + table_name + " " + columns + " " + values + ";";
	}
	
	
	/**
	 * Generate a SQL query to update a table with the information in this course object
	 * 
	 * @param table_name the table to be updated
	 * @return
	 */
	public String generateLocalUpdate(String table_name)
	{
		String statement = "UPDATE " + table_name + " SET ";
		for(Entry<String, String> course : courseInfo.entrySet())
		{
			statement += course.getKey() + " = " + "\'" + course.getValue().replaceAll("[']", "\'\'") + "\'" + ", ";
		}
		statement = statement.substring(0, statement.length()-2); //removing trailing comma and space
		statement += " WHERE crndate = " + courseInfo.get("crndate") + ";";
		return statement;
	}
		
	
	
	
	/******* FOR AWS DYNAMODB *******/
	
	
	
	
	
	
	/**
	 * Used to generate a tuple for insertion to a table on AWS DynamoDB
	 * @return a HashMap of strings and attribute values for DynamoDB, which can be uploaded using the AWS SDK
	 */
	public HashMap<String, AttributeValue> generateItemPush()
	{	
		HashMap<String, AttributeValue> newItems = new HashMap<String, AttributeValue>();
		for(HashMap.Entry<String, String> entry : courseInfo.entrySet())
		{
			newItems.put(entry.getKey(), new AttributeValue(entry.getValue()));
		}
		return newItems;
	}
	
	
	/**
	 * Used to generate an update to a table on AWS DynamoDB
	 * @return a HashMap of strings and attribute value updates for DynamoDB, which can be uploaded using the AWS SDK
	 */
	public HashMap<String, AttributeValueUpdate> generateItemUpdate()
	{
		HashMap<String, AttributeValueUpdate> updates = new HashMap<String, AttributeValueUpdate>();
		for(HashMap.Entry<String, String> entry : courseInfo.entrySet())
		{
			AttributeValue value = new AttributeValue(entry.getValue());
			if(StringUtils.isBlank(value.getS()))
			{
				System.out.println("BLANK ATTRIBUTE" + entry.getKey() + " and Value is " + entry.getValue());
				System.out.println(courseInfo.get("crn"));
			}
			else
				updates.put(entry.getKey(), new AttributeValueUpdate(new AttributeValue(entry.getValue()), AttributeAction.PUT));
		}
		//removing the CRN, which does not change
		updates.remove("crn");
		return updates;
	}
	

	/**
	 * 
	 * @return the primary key for a tuple on AWS DynamoDB
	 */
	public HashMap<String, AttributeValue> itemKey()
	{
		HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		AttributeValue crn = new AttributeValue(courseInfo.get("crn"));
		if(StringUtils.isBlank(crn.getS()))
		{
			System.out.println("BLANK CRN");
			return null;
		}
		key.put("crn", crn);
		return key;
	}
	
	
	
	
	
	/******* DEBUGGING *******/
	
	
	
	public String printInfo()
	{
		String info = "";
		for(Map.Entry<String, String> curInfo : courseInfo.entrySet())
		{
			info += (curInfo.getKey() + ": " + curInfo.getValue());
			if(curInfo.getValue() == "" || curInfo.getValue().matches("[\\s]*") || curInfo.getValue().length() < 1){
				for(int i = 0; i < 1000; i++)
					info += curInfo.getKey();
				break;
			}
			
			info += (" | ");
			
		}
		return info;
	}
	
}
