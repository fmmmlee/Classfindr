/*
 * 
 * Matthew Lee
 * Spring 2019
 * Classfindr
 * Course Class
 * 
 */


import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;

import software.amazon.awssdk.utils.StringUtils;


public class Course {
	
	public final HashMap<String, String> courseInfo = new HashMap<String, String>();

	//default constructor
	public Course() {}
	
	
	/*
	 * 
	 * Creating a table entry for DynamoDB
	 * 
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
	
	
	/*
	 * 
	 * Creates Item Update
	 * 
	 */
	public HashMap<String, AttributeValueUpdate> generateItemUpdate()
	{
		HashMap<String, AttributeValueUpdate> updates = new HashMap<String, AttributeValueUpdate>();
		for(HashMap.Entry<String, String> entry : courseInfo.entrySet())
		{
			updates.put(entry.getKey(), new AttributeValueUpdate(new AttributeValue(entry.getValue()), AttributeAction.PUT));
		}
		//removing the CRN, which does not change
		updates.remove("crn");
		return updates;
	}
	
	/*
	 * 
	 * Creates Item Key
	 * 
	 */	
	public HashMap<String, AttributeValue> itemKey()
	{
		HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("crn", new AttributeValue(courseInfo.get("crn")));
		return key;
	}
	
	
	/*
	 * 
	 * Debugging
	 * 
	 */
	public void printInfo()
	{
		for(Map.Entry<String, String> curInfo : courseInfo.entrySet())
		{
			
			System.out.print(curInfo.getKey() + ": " + curInfo.getValue());
			if(curInfo.getValue() == "" || curInfo.getValue().matches("[\\s]*") || curInfo.getValue().length() < 1){
				System.out.println("empty space at " + curInfo.getKey());
				break;
			}
			
			System.out.print(" | ");
			
		}
		
		System.out.println();
	}
	
}
