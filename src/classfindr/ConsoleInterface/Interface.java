package classfindr.ConsoleInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import classfindr.Utility.Preferences;
import static classfindr.Utility.Constants.*;

public class Interface {

	/* initial user input */
	public static void setprefs(Preferences to_be_set)
	{
		Scanner input = new Scanner(System.in);
		System.out.println(SYSMSG + "Data is available from Fall 2003 to mid-2020.");
		System.out.println(SYSMSG + "Modes: INSERT or UPDATE");
		System.out.println(SYSMSG + "Databases: AWS or EMBEDDED");
		System.out.println(SYSMSG + "Example Command: 2012 Fall to 2013 Summer UPDATE AWS courses");
		System.out.print(SYSMSG + "Input format: [YEAR] [TERM] to [YEAR] [TERM] [MODE] [DATABASE] [TABLE]\n" + INPUT + " ");
		String[] prefs = input.nextLine().split(" ");
		List<String> temp_terms = new ArrayList<String>();
		int current_year = 100*(Integer.parseInt(prefs[0]));
		int year_end = 100*(Integer.parseInt(prefs[3]));
		
		switch(prefs[1].toLowerCase())
		{
		case "winter" :
			current_year += 10;
			break;
		case "spring" :
			current_year += 20;
			break;
		case "summer" :
			current_year += 30;
			break;
		case "fall" :
			current_year += 40;
			break;
		}
		
		switch(prefs[4].toLowerCase())
		{
		case "winter" :
			year_end += 10;
			break;
		case "spring" :
			year_end += 20;
			break;
		case "summer" :
			year_end += 30;
			break;
		case "fall" :
			year_end += 40;
			break;
		}
		
		switch(prefs[5])
		{
		case "UPDATE" :
			to_be_set.mode = 2;
			break;
		case "INSERT" :
			to_be_set.mode = 1;
			break;
		}
		
		switch(prefs[6])
		{
		case "AWS" :
			to_be_set.database = 1;
			break;
		case "EMBEDDED" :
			to_be_set.database = 0;
			break;
		}
		
		to_be_set.table = prefs[7];
		
		while(current_year != year_end)
		{
			temp_terms.add(String.valueOf(current_year));
			current_year += 10;
			if(current_year % 100 > 40)
			{
				current_year += 100;
				current_year -= 40;
			}
		}
		
		to_be_set.terms = temp_terms.toArray(new String[temp_terms.size()]);
		input.close();
	}
	
	
}
