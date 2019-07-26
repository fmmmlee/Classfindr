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
	 * @deprecated doesn't function properly
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
	 * Creates a table if it does not already exist
	 * @param table_name the name of the table to create
	 * @param conn the database connection over which to create a table
	 * @throws SQLException
	 */
	
	public static void createTable(String table_name, Connection conn) throws SQLException
	{
		//TODO: Change to be identical to the names seen on AWS
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
