/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		int in_doc_id;
		//ID SHOULD BE ASSINGED AUTOMATICALLY
		
		String in_doc_name;
		do {
			System.out.print("\tInsert Doctor Name: ");
			try { // read the string, parse it and break.
				in_doc_name = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);

		String in_spec;
		do {
			System.out.print("\tInsert Doctor's Specialty: ");
			try { // read the string, parse it and break.
				in_spec = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);

		int in_did;
		do {
			System.out.print("\tInsert Doctor's Department ID: ");
			try { // read the integer, parse it and break.
				in_did = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);

                try{
                String query1 = "INSERT INTO Doctor " +
                                "SELECT ";
		String queryID = "SELECT* FROM Doctor;";
		in_doc_id = esql.executeQuery(queryID) + 1;
		query1 += in_doc_id;
		query1 += ", '";
		query1 += in_doc_name + "'";
		query1 += ", '";
		query1 += in_spec + "'";
		query1 += ", ";
		query1 += in_did;
		query1 += "WHERE NOT EXISTS(SELECT* FROM Doctor WHERE doctor_ID = ";
		query1 += in_doc_id;
		query1 += ");";
			
		esql.executeUpdate(query1);
		//System.out.println(in_doc_id);
		
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		

	}


	public static void AddPatient(DBproject esql) {//2
 		int in_p_id;               
		//IDS SHOULD BE ASSIGNED AUTOMATICALLY 
			
		String in_p_name;
                do {
                        System.out.print("\tInsert Patient Name: ");
                        try { // read the integer, parse it and break.
                                in_p_name = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

		String in_gtype;
                do {
                        System.out.print("\tInsert Gender: ");
                        try { // read the integer, parse it and break.
                                in_gtype = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

		int in_age;
                do {
                        System.out.print("\tInsert Age: ");
                        try { // read the integer, parse it and break.
                                in_age = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

		String in_add;
                do {
                        System.out.print("\tInsert Address: ");
                        try { // read the integer, parse it and break.
                                in_add = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

		int in_no_of_appts = 0;
                /*do {
                        System.out.print("\tInsert Number of Appointments: ");
                        try { // read the integer, parse it and break.
                                in_no_of_appts = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);*/
		
		try{
		String query2 = "INSERT INTO Patient " +
				"SELECT ";
		String queryID = "SELECT* FROM Patient;";
		in_p_id = esql.executeQuery(queryID) + 1;
		query2 += in_p_id;
		query2 += ", '";
		query2 += in_p_name + "'";
		query2 += ", '";
		query2 += in_gtype + "'";
		query2 += ", ";
		query2 += in_age;
		query2 += ", '";
		query2 += in_add + "'";
		query2 += ", ";
		query2 += in_no_of_appts;
		query2 += "WHERE NOT EXISTS(SELECT* FROM Patient WHERE patient_ID = ";
		query2 += in_p_id;
		query2 += ");";

		esql.executeUpdate(query2);
		//System.out.println(in_p_id);
		//esql.getCurrSeqVal(query2);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void AddAppointment(DBproject esql) {//3
                int in_appnt_id;
                //IDS SHOULD BE ASSIGNED AUTOMATICALLY

                String in_adate;
                do {
                        System.out.print("\tInsert Appointment date: ");
                        try { // read the integer, parse it and break.
                                in_adate = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

                String in_time_slot;
                do {
                        System.out.print("\tInsert Time slot: ");
                        try { // read the integer, parse it and break.
                                in_time_slot = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

                String in_status;
                do {
                        System.out.print("\tInsert Appintment status: ");
                        try { // read the integer, parse it and break.
                                in_status = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);
/*
                String in_add;
                do {
                        System.out.print("\tInsert Address: ");
                        try { // read the integer, parse it and break.
                                in_add = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);
                do {
                        System.out.print("\tInsert Number of Appointments: ");
                        try { // read the integer, parse it and break.
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);
*/
                try{
                String query3 = "INSERT INTO Appointment " +
                                "SELECT ";
                String queryID = "SELECT* FROM Appointment;";
                in_appnt_id = esql.executeQuery(queryID) + 1;
                query3 += in_appnt_id;
                query3 += ", '";
                query3 += in_adate + "'";
                query3 += ", '";
                query3 += in_time_slot + "'";
                query3 += ", '";
                query3 += in_status + "'";
                query3 += "WHERE NOT EXISTS(SELECT* FROM Appointment WHERE appnt_ID = ";
                query3 += in_appnt_id;
                query3 += ");";

                esql.executeUpdate(query3);
                //System.out.println(in_appnt_id);
                //esql.getCurrSeqVal(query2);
                }catch(Exception e) {
                        System.out.println(e.getMessage());
                }
        }	


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB


                int in_p_id;
                //IDS SHOULD BE ASSIGNED AUTOMATICALLY 

                String in_p_name;
                do {
                        System.out.print("\tInsert Patient Name: ");
                        try { // read the integer, parse it and break.
                                in_p_name = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

                String in_gtype;
                do {
                        System.out.print("\tInsert Gender: ");
                        try { // read the integer, parse it and break.
                                in_gtype = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

		int in_age;
                do {
                        System.out.print("\tInsert Age: ");
                        try { // read the integer, parse it and break.
                                in_age = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);

                String in_add;
                do {
                        System.out.print("\tInsert Address: ");
                        try { // read the integer, parse it and break.
                                in_add = in.readLine();
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);
		
		int in_no_of_appts = 0;		

                /*int in_appnt_id;
                do {
                        System.out.print("\tAppointment ID: ");
                        try { // read the integer, parse it and break.
                                in_appnt_id = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e) {
                                System.out.println("Your input is invalid!");
                                continue;
                        }//end try
                }while (true);
*/
                try{
                String query2 = "INSERT INTO Patient " +
                                "SELECT ";
                String queryID = "SELECT* FROM Patient;";
                in_p_id = esql.executeQuery(queryID) + 1;
                query2 += in_p_id;
                query2 += ", '";
                query2 += in_p_name + "'";
                query2 += ", '";
                query2 += in_gtype + "'";
                query2 += ", ";
                query2 += in_age;
                query2 += ", '";
                query2 += in_add + "'";
                query2 += ", ";
                query2 += in_no_of_appts;
                query2 += "WHERE NOT EXISTS(SELECT* FROM Patient WHERE patient_ID = ";
                query2 += in_p_id;
                query2 += ");";

                esql.executeUpdate(query2);

		String query4_2 = "SELECT* FROM Patient WHERE patient_ID = ";	
		query4_2 += in_p_id + ";";

		esql.executeQueryAndPrintResult(query4_2);

		String query4_3 = "UPDATE Patient SET number_of_appts = ";	
		query4_3 += in_no_of_appts + 1;
		query4_3 += "WHERE patient_ID = ";
		query4_3 += in_p_id;
		query4_3 += "AND EXISTS(SELECT* FROM Patient P WHERE EXISTS(SELECT* FROM Patient P2 WHERE P.patient_ID = "; 
		query4_3 += in_p_id;
		query4_3 += " AND P2.patient_ID = ";
		query4_3 += in_p_id;
		query4_3 += "));";
	
		esql.executeUpdate(query4_3);


                String querya = "SELECT* FROM Patient WHERE patient_ID = ";
                querya += in_p_id + ";";
				
		esql.executeQueryAndPrintResult(querya);
//AC->WL
		String query4_a = "SELECT* FROM Appointment WHERE appnt_ID = ";
                System.out.print("\tAppointment ID: ");
                int in_appnt_id = Integer.parseInt(in.readLine());
		query4_a += in_appnt_id;
		query4_a += ";";
		

		
		esql.executeQueryAndPrintResult(query4_a);		

                String status1 = "WL";
                String status2 = "AC";

		String query4_4 = "UPDATE Appointment SET status = '";
		query4_4 += status1 + "'";
		query4_4 += "WHERE appnt_ID = ";
		query4_4 += in_appnt_id;
		query4_4 += "AND status = '";
		query4_4 += status2 + "'";
		query4_4 += "AND status =(SELECT A.status FROM Appointment A, has_appointment HA, Doctor Dr WHERE A.status = '";
		query4_4 += status2 + "'";
		query4_4 += "AND A.appnt_ID = ";
		query4_4 += in_appnt_id;
		query4_4 += "AND HA.appt_ID = ";
		query4_4 += in_appnt_id;
		query4_4 += "AND HA.doctor_ID = ";
		System.out.print("\tDoctor ID: ");
		int in_doc_id = Integer.parseInt(in.readLine());
		query4_4 += in_doc_id;
		query4_4 += "AND Dr.doctor_ID = ";
		query4_4 += in_doc_id;
		query4_4 += ");"; 

                esql.executeUpdate(query4_4);
               // esql.executeQueryAndPrintResult(query4_4);				

		String query4_4b = "SELECT* FROM Appointment WHERE appnt_ID = ";
		query4_4b += in_appnt_id;
		query4_4b += ";";
		
		//esql.executeUpdate(query4_4);
                esql.executeQueryAndPrintResult(query4_4b);


//AV->AC

                String query4_5a = "SELECT* FROM Appointment WHERE appnt_ID = ";
                System.out.print("\tAppointment ID: ");
                int in_appnt_id_5 = Integer.parseInt(in.readLine());
                query4_5a += in_appnt_id_5;
                query4_5a += ";";

                esql.executeQueryAndPrintResult(query4_5a);

                String status1_5 = "AC";
                String status2_5 = "AV";

                String query4_5 = "UPDATE Appointment SET status = '";
                query4_5 += status1_5 + "'";
                query4_5 += "WHERE appnt_ID = ";
                query4_5 += in_appnt_id_5;
                query4_5 += "AND status = '";
                query4_5 += status2_5 + "'";
                query4_5 += "AND status =(SELECT A.status FROM Appointment A, has_appointment HA, Doctor Dr WHERE A.status = '";
                query4_5 += status2_5 + "'";
                query4_5 += "AND A.appnt_ID = ";
                query4_5 += in_appnt_id_5;
                query4_5 += "AND HA.appt_ID = ";
                query4_5 += in_appnt_id_5;
                query4_5 += "AND HA.doctor_ID = ";
                System.out.print("\tDoctor ID: ");
                int in_doc_id_5 = Integer.parseInt(in.readLine());
                query4_5 += in_doc_id_5;
                query4_5 += "AND Dr.doctor_ID = ";
                query4_5 += in_doc_id_5;
                query4_5 += ");";

                esql.executeUpdate(query4_5);
               // esql.executeQueryAndPrintResult(query4_4);                            

                String query4_5b = "SELECT* FROM Appointment WHERE appnt_ID = ";
                query4_5b += in_appnt_id_5;
                query4_5b += ";";

                //esql.executeUpdate(query4_4);
               esql.executeQueryAndPrintResult(query4_5b);


		}catch(Exception e) {
			System.out.println(e.getMessage());
		} 

	}











		public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
                //System.out.print("\tEnter Doctor ID: ");
                int in_doc_id;
            	 do{
			System.out.print("\tEnter Doctor ID: ");
                	try{
                        	in_doc_id = Integer.parseInt(in.readLine());
                        	break;
			}catch(Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while(true);
		
		String in_date1;
		do{
			System.out.print("\tEnter start date: ");
			try{
				in_date1 = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while(true);
		

		String in_date2;
		do{
			System.out.print("\tEnter end date: ");
			try{
				in_date2 = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while(true);		

		try{
		String query5 = "SELECT A.appnt_id, A.adate, A.time_slot, A.status " +
				"FROM Doctor Dr, Appointment A, has_appointment HA " +
				"WHERE Dr.doctor_ID = HA.doctor_id AND HA.appt_id = A.appnt_ID AND (A.status = 'AV' OR A.status = 'AC') AND Dr.doctor_ID = ";
		
		query5 += in_doc_id;
		
		query5 += "AND (A.adate BETWEEN '";
		
		query5 += in_date1 + "'";
	
		query5 += "AND '";

		query5 += in_date2 + "'";	
		
		query5 += ");";

		esql.executeQueryAndPrintResult(query5);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		String in_dname;
		do{
			System.out.print("\tEnter Department Name: ");
			try{
				in_dname = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}	
		}while(true);
	
		String in_date;
		do{
			System.out.print("\tEnter Date: ");
			try{
				in_date = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while (true);
		
		try{
		String query6 = "SELECT A.status, A.adate, A.appnt_ID " +
				"FROM Appointment A, has_appointment HA " +
				"WHERE A.appnt_ID = HA.appt_id AND A.status = 'AV' AND A.adate = '";

		query6 += in_date + "'";
	
		query6 += "AND EXISTS(SELECT* " +
			  "FROM Doctor Dr " +
			  "WHERE Dr.doctor_ID = HA.doctor_ID " +
			  	"AND EXISTS(SELECT D.name " +
					   "FROM Department D " +
					   "WHERE D.dept_ID = Dr.did AND D.name = '";
		query6 += in_dname + "'";
		query6 += "));";

		esql.executeQueryAndPrintResult(query6);
		}catch (Exception e){
			System.err.println(e.getMessage());
		}	

	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		try{
		String query7 = "SELECT Dr.name, A.status, COUNT(A.appnt_id) " +
				"FROM Doctor Dr, Appointment A, has_appointment HA " +
				"WHERE HA.doctor_ID = Dr.doctor_ID AND HA.appt_ID = A.appnt_ID " +
					"AND EXISTS(SELECT COUNT(A1.status) AS differentTypes " +
						   "FROM Appointment A1, Appointment A2 " +
						   "WHERE A1.appnt_ID = A2.appnt_ID " +
						   "ORDER BY differentTypes) " +
				"GROUP BY Dr.name, A.status " +
				"ORDER BY COUNT(A.appnt_id) desc;";
		esql.executeQueryAndPrintResult(query7);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		String in_status;
		do{	
			System.out.print("\tEnter Status (PA/AC/AV/WL): ");
			try{
				in_status = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		}while(true);

		try{
		String query8 = "SELECT Dr.name, A.status, COUNT(P.patient_ID) " +
				"FROM Patient P, Searches S, Appointment A, Doctor Dr, has_appointment HA " +
				"WHERE P.patient_ID = S.pid AND A.appnt_ID = S.aid AND Dr.doctor_ID = HA.doctor_ID AND A.status = '";
		query8 += in_status + "'";
		//esql.executeQuery(query8);
		
		query8 += "GROUP BY Dr.name, A.status;";
		
		//esql.executeQuery(query8);		
		esql.executeQueryAndPrintResult(query8);	
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
//}

	public static void indexes(DBproject esql) {
	try{
		String index1 = "CREATE INDEX index_patient ON Patient USING BTREE (patient_ID);";
		String index2 = "CREATE INDEX index_doctor on Doctor USING BTREE (doctor_ID);";
		String index3 = "CREATE INDEX index_dept ON Department USING BTREE (dept_ID);";
		String index4 = "CREATE INDEX index_appt ON Appointment USING BTREE (appnt_ID);";
		String index5 = "CREATE INDEX index_HA on has_appointment USING BTREE (appt_id);";

		esql.executeQuery(index1);
		esql.executeQuery(index2);
                esql.executeQuery(index3);
                esql.executeQuery(index4);
		esql.executeQuery(index5);
                }catch(Exception e){
                        System.err.println(e.getMessage());
                }
	}
}	
