package classfindr.Threads.DatabaseAccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author Matthew Lee
 *
 * Some helper functions for use in AccessLocalDB and elsewhere
 */


public class LocalDBTools {
	
	
	/**
	 * Creates a table if it does not already exist
	 * @param table_name the name of the table to create
	 * @param conn the database connection over which to create a table
	 * @throws SQLException
	 */
	//TODO: Make this a generic function that accepts a table name, list of values, and constraints
	public static void createTable(String table_name, Connection conn) throws SQLException
	{
		String table_query_str = "CREATE TABLE IF NOT EXISTS " + table_name + "("
				+ "end_date char(5),"
				+ "crndate varchar(15),"
				+ "year char(4),"
				+ "subject varchar(10),"
				+ "extra_charges varchar(500),"
				+ "available varchar(5),"
				+ "end_time varchar(500),"
				+ "description varchar(2000),"
				+ "building varchar(500),"
				+ "capacity varchar(5),"
				+ "meet_times varchar(500),"
				+ "number varchar(10),"
				+ "start_time varchar(500),"
				+ "instructor varchar(500),"
				+ "credits varchar(10),"
				+ "term varchar(10),"
				+ "crn varchar(15),"
				+ "attributes varchar(500),"
				+ "enrolled varchar(5),"
				+ "start_date char(5),"
				+ "prerequisites varchar(1000),"
				+ "restrictions varchar(1000)," 
				+ "waitlist varchar(5),"
				+ "PRIMARY KEY(crndate)"
				+ ");";
		
		Statement table_query = conn.createStatement();
		table_query.execute(table_query_str);
	}
	
}
