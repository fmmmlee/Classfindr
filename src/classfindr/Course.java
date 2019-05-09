package classfindr;
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

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;


public class Course{
	
	public final HashMap<String, String> courseInfo = new HashMap<String, String>();

	//default constructor
	public Course(){}
	
	
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
	
	/*
	 * 
	 * Creates Item Key
	 * 
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
	
	
	/*
	 * 
	 * Debugging
	 * 
	 */
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
