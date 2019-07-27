package classfindr.Utility;

/*
 * 
 * Matthew Lee
 * Summer 2019
 * Classfindr
 * Stores information about application preferences defined by user.
 * 
 * 
 */

public class Preferences
{
	public String table;				//name of table
	public String[] terms = null;		//array of school terms being processed
	public int mode;					//determines database write type --- 1 = insert, 2 = update
	public int database;				//determines destination of write --- 0 = embedded database, 1 = AWS DynamoDB
	
	public String modeStr()	//return string representation of upload mode
	{
		switch(mode)
		{
		case 1:
			return "INSERT";
		case 2:
			return "UPDATE";
		default:
			return "";
		}
	}
	
	public String dbStr()	//return string representation of database type
	{
		switch(database)
		{
		case 0:
			return "EMBEDDED";
		case 1:
			return "AWS";
		default:
			return "";
		}
	}
}