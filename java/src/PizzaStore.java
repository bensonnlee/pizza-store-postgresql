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
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // Field to store the currently logged in user.
   private String currentUser;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
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
      stmt.close();
      return rowCount;
   }//end executeQuery

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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
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
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
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
	if (rs.next())
		return rs.getInt(1);
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

   // Setter for the current user.
   public void setCurrentUser(String user) {
      this.currentUser = user;
   }

   // Getter for the current user.
   public String getCurrentUser() {
      return this.currentUser;
   }

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
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

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      try {
         String login, password, role, favoriteItems, phoneNum;
         // get user login input
         System.out.print("Enter login: ");
         login = in.readLine().trim();

         // validate login
         if (login.length() > 50 || login.isEmpty()) {
            System.out.println("Error: Login must be between 1 and 50 characters.");
            return;
         }

         // check for unique login
         String checkQuery = String.format("SELECT login FROM Users WHERE login = '%s';", login);
         int count = esql.executeQuery(checkQuery);
         if (count > 0) {
            System.out.println("Error: Login already exists. Please choose a different login.");
            return;
         }

         // get user password input
         System.out.print("Enter password: ");
         password = in.readLine().trim();

         // validate password
         if (password.length() > 30 || password.isEmpty()) {
            System.out.println("Error: Password must be between 1 and 30 characters.");
            return;
         }

         // get user phone number input
         System.out.print("Enter phone number: ");
         phoneNum = in.readLine().trim();
         
         // validate phone number
         String phoneRegex = "^\\d{3}-\\d{3}-\\d{4}$";
         if (phoneNum.isEmpty()) {
            System.out.println("Error: Phone number cannot be empty.");
            return;
         }
         if (!phoneNum.matches(phoneRegex)) {
            System.out.println("Error: Phone number must be in the format XXX-XXX-XXXX.");
            return;
         }

         // set role and favorite item
         role = "customer";
         favoriteItems = "";

         // build SQL statement 
         String query = String.format(
            "INSERT INTO Users (login, password, role, favoriteItems, phoneNum)" +
            "VALUES ('%s', '%s', '%s', '%s', '%s');",
            login, password, role, favoriteItems, phoneNum);

         // execute SQL statement
         esql.executeUpdate(query);
         System.out.println("User created successfully!");

      } catch(Exception e) {
         System.err.println("An error occurred while creating user: " + e.getMessage());
      }
   }//end CreateUser

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try {
         String user_login, password;
         // get user login input
         System.out.print("Enter login: ");
         user_login = in.readLine().trim();

         // validate login
         if (user_login.length() > 50 || user_login.isEmpty()) {
            System.out.println("Error: Login must be between 1 and 50 characters.");
            return null;
         }

         System.out.print("Enter password: ");
         password = in.readLine().trim();

         // validate password
         if (password.length() > 50 || password.isEmpty()) {
            System.out.println("Error: Password must be between 1 and 50 characters.");
            return null;
         }

         String password_query = String.format("SELECT login, password FROM Users WHERE login = '%s' AND password = '%s';", user_login, password);
         int count = esql.executeQuery(password_query);
         if (count <= 0) {
            System.out.println("Error: User does not exists. Incorrect login or password.");
            return null;
         }
         esql.setCurrentUser(user_login);
         return user_login;
      }catch(Exception e) {
         System.err.println("An error occurred while creating user: " + e.getMessage());
      }

      return null;
   }//end LogIn

// Rest of the functions definition go in here

   public static void printProfileHelper(PizzaStore esql, String login) {
      System.out.println("\n=== Profile for " + login + " ===");
      try {
         // Construct SQL query to fetch the user's profile information.
         String query = String.format("SELECT favoriteItems, phoneNum, role FROM Users WHERE login = '%s';", login);
         
         // Retrieve the result as a list of records.
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         List<String> row = results.get(0);
         String favoriteItems = row.get(0);
         String phoneNum = row.get(1);
         String role = row.get(2);
         
         // Prepare a formatted table output.
         String line = "+----------------------+--------------------------+";
         System.out.println(line);
         System.out.printf("| %-20s | %-24s |\n", "Field", "Value");
         System.out.println(line);
         System.out.printf("| %-20s | %-24s |\n", "Favorite Items", favoriteItems);
         System.out.printf("| %-20s | %-24s |\n", "Phone Number", phoneNum);
         System.out.printf("| %-20s | %-24s |\n", "Role", role);
         System.out.println(line);
     } catch (Exception e) {
         System.err.println("An error occurred while viewing profile: " + e.getMessage());
     }
   }

   public static void viewProfile(PizzaStore esql) {
      String login = esql.getCurrentUser();
      printProfileHelper(esql, login);
   }
   
   public static void updateProfile(PizzaStore esql) {}
   public static void viewMenu(PizzaStore esql) {}
   public static void placeOrder(PizzaStore esql) {}
   public static void viewAllOrders(PizzaStore esql) {}
   public static void viewRecentOrders(PizzaStore esql) {}
   public static void viewOrderInfo(PizzaStore esql) {}
   public static void viewStores(PizzaStore esql) {}
   public static void updateOrderStatus(PizzaStore esql) {}
   public static void updateMenu(PizzaStore esql) {}
   
   /*
      Update a user's login and role as a manager
   */
   public static void updateUser(PizzaStore esql) {
      try {
         String manager_login = esql.getCurrentUser();

         //check if role is manager
         String check_role_query = String.format("SELECT * FROM Users WHERE login = '%s' AND role = 'manager'", manager_login);
         if (esql.executeQuery(check_role_query) < 1) {
            System.out.println("Error: Access denied. You must be a manager to update a user.");
            return;
         }

         //Pick user you want to update
         System.out.print("Enter the login of the user you want to update: ");
         String update_login = in.readLine().trim();

         //check if user exists
         String user_exists = String.format("SELECT * FROM Users WHERE login = '%s'", update_login);
         if (esql.executeQuery(user_exists) < 1) {
            System.out.println("Error: User " + update_login + " does not exist\n");
            return;
         }

         boolean update_menu = true;
         while (update_menu) {
             //Update menu
            System.out.println("UPDATE MENU FOR USER: " + update_login);
            System.out.println("-----------");
            System.out.println("1. Update user's login");
            System.out.println("2. Update user's role");
            System.out.println("9. < EXIT");

            switch(readChoice()) {
               case 1: //new login validation
                  System.out.print("New user login: ");
                  String new_login = in.readLine().trim();
                  String check_new_login = String.format("SELECT * FROM Users WHERE login = '%s'", new_login);
                  if (esql.executeQuery(check_new_login) > 0) {
                     System.out.println("User login already exists. Please pick another one");
                     break;
                  }
                  String update_login_query = String.format("UPDATE Users SET login = '%s' WHERE login = '%s'", new_login, update_login);
                  esql.executeUpdate(update_login_query);
                  update_login = new_login; // Update the login identifier for further operations.
                  System.out.println("User login updated successfully. New profile:");
                  printProfileHelper(esql, update_login);
                  break;
               case 2: //new user role with numeric selection
                  //read in new role
                  System.out.println("Select new role:");
                  System.out.println("1. Customer");
                  System.out.println("2. Manager");
                  System.out.println("3. Driver");
                  int roleChoice;
                  try {
                     roleChoice = Integer.parseInt(in.readLine().trim());
                  } catch(NumberFormatException e) {
                     System.out.println("Invalid input. Please enter a number.");
                     break;
                  }
                  String new_role = "";
                  switch(roleChoice) {
                     case 1:
                        new_role = "customer";
                        break;
                     case 2:
                        new_role = "manager";
                        break;
                     case 3:
                        new_role = "driver";
                        break;
                     default:
                        System.out.println("Invalid choice. Please select a valid role.");
                        break;
                  }
                  if(new_role.equals("")) {
                     break;
                  }
                  String update_role_query = String.format("UPDATE Users SET role = '%s' WHERE login = '%s'", new_role, update_login);
                  esql.executeUpdate(update_role_query);
                  System.out.println("User role updated successfully. New profile:");
                  printProfileHelper(esql, update_login);
                  break;
               case 9: 
                  update_menu = false;
                  break;
               default : System.out.println("Unrecognized choice!"); break;
            }
         }

         return;

      }catch(Exception e) {
         System.err.println("An error occured when while updating a user: " + e.getMessage());
      }
   }//end updateUser


}//end PizzaStore

