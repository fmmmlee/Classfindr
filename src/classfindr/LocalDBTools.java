package classfindr;

import java.sql.Connection;
import java.sql.ResultSet;
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
	 * 
	 * @param table_name the name of the table to check existence
	 * @param conn the connection to check for a table with the name provided
	 * @return a boolean: TRUE if a table with the name provided exists at the connection specified, FALSE otherwise
	 * @throws SQLException
	 */
	public static boolean table_exist(String table_name, Connection conn) throws SQLException
	{
		ResultSet meta = conn.getMetaData().getTables(null, null, table_name, null);
		if(!meta.next())
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param table_name the name of the table to create
	 * @param conn the database connection over which to create a table
	 * @throws SQLException
	 */
	
	public static void create_course_table(String table_name, Connection conn) throws SQLException
	{
		//TODO: Change to be identical to the names seen on AWS
		String table_query_str = "CREATE TABLE " + table_name + "("
				+ "crndate int,"
				+ "crn int,"
				+ "available int,"
				+ "capacity int,"
				+ "enrolled int,"
				+ "description varchar(500),"
				+ "start_date char(5),"
				+ "end_date char(5),"
				+ "instructor varchar(100),"
				+ "number int,"
				+ "subject varchar(10),"
				+ "term varchar(10),"
				+ "year int,"
				+ "building varchar(10),"
				+ "meet_times varchar(10),"
				+ "credits int,"
				+ "prereqs varchar(500),"
				+ "start_time int,"
				+ "end_time int,"
				+ "restr varchar(100)," 
				+ "extr_chgs varchar(100),"
				+ "PRIMARY KEY(crndate)"
				+ ");";
		
		Statement table_query = conn.createStatement();
		table_query.execute(table_query_str);
	}
	
}
