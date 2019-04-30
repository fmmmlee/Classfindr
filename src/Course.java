//Matthew Lee
//Spring 2019
public class Course {
	int crn;
	int year;
	String subject;
	int number;
	String description;
	String term;
	int capacity;
	int enrolled;
	int available;
	String instructor;
	String startdate;
	String enddate;
	String attr;
	String meettimes;
	String building;
	int credits;
	String extrachgs;
	
	public String generateValueStr()
	{
		return "VALUES ("
	+ subject + ", "
	+ number + ", "
	+ year + ", "
	+ term + ", "
	+ capacity + ", " 
	+ enrolled + ", "
	+ available + ", "
	+ instructor + ", "
	+ startdate + ", "
	+ enddate + ", "
	+ attr + ", "
	+ meettimes + ", "
	+ building + ", "
	+ credits + ", "
	+ extrachgs
	+ ")";
	}
}
