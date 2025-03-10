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

   /**
    * Function to create a new user
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

   /**
    * Check log in credentials for an existing user
    * @param esql the PizzaStore object
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

   /**
    * Helper function to print the profile of a user.
    * @param login the login of the user to print the profile of
    **/
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

   /**
    * View the profile of the current user.
    * @param esql the PizzaStore object
    */
   public static void viewProfile(PizzaStore esql) {
      String login = esql.getCurrentUser();
      printProfileHelper(esql, login);
   }
   
   /**
    * Update the profile of the current user.
    */
   public static void updateProfile(PizzaStore esql) {
      String login = esql.getCurrentUser();
      System.out.println("\n=== Profile Update Menu for " + login + " ===");
      boolean update_menu = true;
      while (update_menu) {
         System.out.println("1. Update favorite item");
         System.out.println("2. Update phone number");
         System.out.println("3. Update password");
         System.out.println("9. < Return to main menu");

         switch (readChoice()) {
            case 1: //update favorite items
               try {
                  String favoriteItem;
                  System.out.print("Select your favorite item:\n");

                  // fetch all items from the Items table
                  List<List<String>> items = esql.executeQueryAndReturnResult("SELECT itemName FROM Items;");
                  for (int i = 0; i < items.size(); i++) {
                     System.out.println((i + 1) + ". " + items.get(i).get(0));
                  }

                  // get user choice and check for validation
                  int choice = readChoice();
                  if (choice < 1 || choice > items.size()) {
                     System.out.println("Invalid choice. Please select a valid item.");
                     break;
                  }
                  favoriteItem = items.get(choice - 1).get(0);

                  // update the user's favorite item
                  String update_query = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';", favoriteItem, login);
                  esql.executeUpdate(update_query);
                  System.out.println("Favorite item updated successfully to " + favoriteItem + ".");
               } catch (Exception e) {
                  System.err.println("An error occurred while updating favorite items: " + e.getMessage());
               }
               break;
            case 2: //update phone number
               try {
                  String phoneNum;
                  System.out.print("Enter phone number: ");
                  phoneNum = in.readLine().trim();

                  // validate phone number
                  String phoneRegex = "^\\d{3}-\\d{3}-\\d{4}$";
                  if (phoneNum.isEmpty()) {
                     System.out.println("Error: Phone number cannot be empty.");
                     break;
                  }
                  if (!phoneNum.matches(phoneRegex)) {
                     System.out.println("Error: Phone number must be in the format XXX-XXX-XXXX.");
                     break;
                  }

                  String update_query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';", phoneNum, login);
                  esql.executeUpdate(update_query);
                  System.out.println("Phone number updated successfully.");
               } catch (Exception e) {
                  System.err.println("An error occurred while updating phone number: " + e.getMessage());
               }
               break;
            case 3: //update password
               try {
                  String password;
                  System.out.print("Enter new password: ");
                  password = in.readLine().trim();

                  // validate password
                  if (password.length() > 50 || password.isEmpty()) {
                     System.out.println("Error: Password must be between 1 and 50 characters.");
                     break;
                  }

                  String update_query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';", password, login);
                  esql.executeUpdate(update_query);
                  System.out.println("Password updated successfully.");
               } catch (Exception e) {
                  System.err.println("An error occurred while updating password: " + e.getMessage());
               }
               break;
            case 9:
               update_menu = false;
               break;
            default:
               System.out.println("Unrecognized choice!");
               break;
         }
      }
   }

   /**
    * Allows the user to view all the items on the menu.
    * Users can filter by item type (using the typeOfItem column) and/or
    * by a maximum price. They can also choose to sort the results by price 
    * in ascending (lowest to highest) or descending (highest to lowest) order.
    * The results are displayed in a nicely formatted table.
    */
   public static void viewMenu(PizzaStore esql) {
      try {
         // Prompt for filtering by item type
         System.out.println("Select item type to filter by or press Enter to skip:");
         System.out.println("1. Entree");
         System.out.println("2. Sides");
         System.out.println("3. Drinks");
         String typeChoice = in.readLine().trim();
         String typeFilter = "";
         if (!typeChoice.isEmpty()) {
            try {
               int choice = Integer.parseInt(typeChoice);
               switch(choice) {
                     case 1: 
                        typeFilter = "entree";
                        break;
                     case 2: 
                        typeFilter = "sides";
                        break;
                     case 3: 
                        typeFilter = "drinks";
                        break;
                     default:
                        System.out.println("Invalid choice. No type filter will be applied.");
                        break;
               }
            } catch (NumberFormatException e) {
                  System.out.println("Invalid input. No type filter will be applied.");
            }
         }

         // Prompt for filtering by maximum price
         System.out.print("Enter maximum price to filter by or press Enter to skip: ");
         String priceInput = in.readLine().trim();
         Double maxPrice = null;
         if (!priceInput.isEmpty()) {
            try {
                  maxPrice = Double.parseDouble(priceInput);
            } catch (NumberFormatException nfe) {
                  System.out.println("Invalid price entered. Price filter will be ignored.");
            }
         }

         // Prompt for sorting option
         System.out.println("Sort by price? (Press Enter to skip sorting)");
         System.out.println("1. Ascending (lowest to highest)");
         System.out.println("2. Descending (highest to lowest)");
         String sortInput = in.readLine().trim();
         int sortChoice = 3; // default: no sorting
         if (!sortInput.isEmpty()) {
            try {
               sortChoice = Integer.parseInt(sortInput);
            } catch (NumberFormatException e) {
               System.out.println("Invalid input. No sorting will be applied.");
               sortChoice = 3;
            }
         }

         // Build the base query using the Items table columns.
         String query = "SELECT itemName, typeOfItem, price, description FROM Items";
         boolean hasFilter = false;
         String whereClause = "";

         // Add item type filter if provided.
         if (!typeFilter.isEmpty()) {
            whereClause = whereClause + " typeOfItem = ' " + typeFilter + "'";
            hasFilter = true;
         }

         // Add price filter if provided.
         if (maxPrice != null) {
            if (hasFilter) {
                  whereClause += " AND";
            }
            whereClause = whereClause + " price <= " + maxPrice;
            hasFilter = true;
         }

         // Append WHERE clause if any filters exist.
         if (hasFilter) {
            query += " WHERE" + whereClause;
         }

         // Append ORDER BY clause based on the user's sorting choice.
         if (sortChoice == 1) {
            query += " ORDER BY price ASC";
         } else if (sortChoice == 2) {
            query += " ORDER BY price DESC";
         }

         // Execute the query and fetch results.
         System.out.println(query);
         List<List<String>> results = esql.executeQueryAndReturnResult(query);

         // If no items found, notify the user.
         if (results.size() == 0) {
            System.out.println("No items match your search criteria.");
            return;
         }

         // Define the formatted table style similar to the profile output.
         String line = "+--------------------------------+-----------------+----------+------------------------------------------+";
         System.out.println(line);
         System.out.printf("| %-30s | %-15s | %-8s | %-40s |\n", "Item Name", "Type", "Price", "Description");
         System.out.println(line);

         // Print each item row in the formatted table.
         for (List<String> row : results) {
            String itemName = row.get(0);
            String itemType = row.get(1);
            String price = row.get(2);
            String description = row.get(3);
            // Truncate description if it's too long.
            if (description.length() > 40) {
                  description = description.substring(0, 37) + "...";
            }
            System.out.printf("| %-30s | %-15s | %-8s | %-40s |\n", itemName, itemType, price, description);
         }
         System.out.println(line);
      } catch (Exception e) {
         System.err.println("An error occurred while viewing the menu: " + e.getMessage());
      }
   }

   /**
    * Place an order for the current user.
    * The user can order any item from the menu. User should first be asked which
    * store they want to order from. User will be asked to input every itemName and quantity
    * (the amount of each item they want) for each item they want to order. The total price of
    * their order should be returned and output to the user. After placing the order, the order
    * information needs to be inserted in the FoodOrder table with a unique orderID (and
    * make sure you include the store they ordered at). Each itemName, orderID, and the
    * corresponding quantity should be inserted into the ItemsInOrder table for every item in
    * the order.
    */
   public static void placeOrder(PizzaStore esql) {
      try {
         // Display available stores in pages of 10 results
         System.out.println("Available Stores:");
         String storeQuery = "SELECT storeID, address, city, state FROM Store;";
         List<List<String>> stores = esql.executeQueryAndReturnResult(storeQuery);
         if (stores.size() == 0) {
            System.out.println("No stores available at the moment.");
            return;
         }

         int totalStores = stores.size();
         int pageSize = 10;
         int currentIndex = 0;

         while (currentIndex < totalStores) {
            // Print a page of stores
            for (int i = currentIndex; i < Math.min(currentIndex + pageSize, totalStores); i++) {
               List<String> store = stores.get(i);
               System.out.printf("StoreID: %s, Address: %s, %s, %s\n",
                                 store.get(0), store.get(1), store.get(2), store.get(3));
            }
            currentIndex += pageSize;
            
            // If there are more stores to display, ask the user if they want to continue.
            if (currentIndex < totalStores) {
               System.out.print("Press ENTER to see more results or type 'q' to quit: ");
               String input = in.readLine().trim();
               if (input.equalsIgnoreCase("q")) {
                     break;
               }
            }
         }
         
         // Ask user to choose a store
         System.out.print("Enter the storeID you want to order from: ");
         String storeInput = in.readLine().trim();
         int storeID = Integer.parseInt(storeInput);
         // Verify store exists
         String checkStore = String.format("SELECT * FROM Store WHERE storeID = %d;", storeID);
         int storeCount = esql.executeQuery(checkStore);
         if (storeCount <= 0) {
               System.out.println("Invalid store ID. Order cancelled.");
               return;
         }
         
         // Prepare to collect order items
         List<String> itemNames = new ArrayList<>();
         List<Integer> quantities = new ArrayList<>();
         
         // Prompt user to add items until they enter a blank item name.
         while (true) {
               // Retrieve all available menu items.
               String menuQuery = "SELECT itemName FROM Items;";
               List<List<String>> menuItems = esql.executeQueryAndReturnResult(menuQuery);
               if (menuItems.size() == 0) {
                  System.out.println("No menu items available at the moment.");
                  break; // or return, depending on the context
               }

               // Display available menu items.
               System.out.println("Available Menu Items:");
               for (int i = 0; i < menuItems.size(); i++) {
                  System.out.println((i + 1) + ". " + menuItems.get(i).get(0));
               }

               // Ask the user to pick an item from the list.
               System.out.print("Select the number corresponding to the item you want to order (or press ENTER to finish): ");
               String input = in.readLine().trim();
               if (input.isEmpty()) {
                  break; // exit the loop if the user is done ordering
               }

               int choice;
               try {
                  choice = Integer.parseInt(input);
               } catch (NumberFormatException e) {
                  System.out.println("Invalid input. Please enter a valid number.");
                  continue; // prompt the user again
               }

               if (choice < 1 || choice > menuItems.size()) {
                  System.out.println("Invalid choice. Please try again.");
                  continue;
               }

               // Retrieve the selected item name.
               String itemName = menuItems.get(choice - 1).get(0);
               System.out.print("Enter quantity for " + itemName + ": ");
               String qtyStr = in.readLine().trim();
               int quantity = Integer.parseInt(qtyStr);
               if (quantity <= 0) {
                  System.out.println("Quantity must be positive. Try again.");
                  continue;
               }
               // Add valid item and quantity to the order lists.
               itemNames.add(itemName);
               quantities.add(quantity);
         }
         
         // Check that at least one item was ordered.
         if (itemNames.size() == 0) {
               System.out.println("No items ordered. Order cancelled.");
               return;
         }
         
         // Calculate the total price for the order.
         double totalPrice = 0.0;
         for (int i = 0; i < itemNames.size(); i++) {
               String query = String.format("SELECT price FROM Items WHERE itemName = '%s';", itemNames.get(i));
               List<List<String>> priceResult = esql.executeQueryAndReturnResult(query);
               double price = Double.parseDouble(priceResult.get(0).get(0));
               totalPrice += price * quantities.get(i);
         }
         
         // Generate a unique orderID by finding the current maximum orderID and adding 1.
         String maxQuery = "SELECT MAX(orderID) FROM FoodOrder;";
         List<List<String>> maxResult = esql.executeQueryAndReturnResult(maxQuery);
         int newOrderID = 1;
         if (maxResult.size() > 0 && maxResult.get(0).get(0) != null) {
               newOrderID = Integer.parseInt(maxResult.get(0).get(0)) + 1;
         }
         
         // Insert the order into the FoodOrder table.
         String currentUser = esql.getCurrentUser();
         String insertOrder = String.format(
               "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " +
               "VALUES (%d, '%s', %d, %.2f, now(), 'incomplete');",
               newOrderID, currentUser, storeID, totalPrice);
         esql.executeUpdate(insertOrder);
         
         // Insert each item in the order into the ItemsInOrder table.
         for (int i = 0; i < itemNames.size(); i++) {
               String insertItem = String.format(
                  "INSERT INTO ItemsInOrder (orderID, itemName, quantity) " +
                  "VALUES (%d, '%s', %d);",
                  newOrderID, itemNames.get(i), quantities.get(i));
               esql.executeUpdate(insertItem);
         }
         
         // Output the total price to the user.
         System.out.println("Order placed successfully!");
         System.out.println("Your total price is: $" + String.format("%.2f", totalPrice));
         
      } catch (Exception e) {
         System.err.println("An error occurred while placing the order: " + e.getMessage());
      }
   }

   /*
      View info about all user's order. Customers can only see their own order info.
      Managers and drivers can view all orders of a specific user.
   */
   public static void viewAllOrders(PizzaStore esql) {
      try {
         //Get role of current user
         String curr_user_login = esql.getCurrentUser();
         String user_role_query = String.format("SELECT role FROM Users WHERE login = '%s'", curr_user_login);
         List<List<String>> user = esql.executeQueryAndReturnResult(user_role_query);
         String user_role = user.get(0).get(0).trim();

         if (user_role.equals(String.format("customer"))) { //if customer, find order with user login
            String food_order_query = 
               String.format("SELECT F.orderID, F.orderTimestamp, F.totalPrice, F.orderStatus, I.itemName, I.quantity FROM FoodOrder F JOIN ItemsInOrder I ON F.orderID = I.orderID GROUP BY F.orderID, I.itemName, I.quantity HAVING F.login = '%s'", 
               curr_user_login);
            List<List<String>> user_orders = esql.executeQueryAndReturnResult(food_order_query);
            if (user_orders.size() <= 0) {
               System.out.println("No orders in history");
               return;
            }

            //turn the all user_orders into List of Lists of each order
            List<List<String>> individual_orders = new ArrayList<>();
            List<String> each_order = new ArrayList<>();
            each_order.add(user_orders.get(0).get(0));
            each_order.add(user_orders.get(0).get(1));
            each_order.add(user_orders.get(0).get(2));
            each_order.add(user_orders.get(0).get(3).trim());
            each_order.add(user_orders.get(0).get(4));
            each_order.add(user_orders.get(0).get(5));

            //grab items in orders order = (orderID, itemName, quantity, itemName, quanitity, ...)
            for (int i = 1; i < user_orders.size(); i++) {
               String orderID = user_orders.get(i).get(0);

               if (!orderID.equals(each_order.get(0))) {
                  individual_orders.add(each_order);
                  each_order = new ArrayList<>();
                  each_order.add(orderID);
                  each_order.add(user_orders.get(i).get(1));
                  each_order.add(user_orders.get(i).get(2));
                  each_order.add(user_orders.get(i).get(3).trim());
               }

               each_order.add(user_orders.get(i).get(4));
               each_order.add(user_orders.get(i).get(5));

            }
            individual_orders.add(each_order);

            //print out the orders 
            for (int i = 0; i < individual_orders.size(); i++) {
               String id = individual_orders.get(i).get(0);
               String timestamp = individual_orders.get(i).get(1);
               String orderPrice = individual_orders.get(i).get(2);
               String status = individual_orders.get(i).get(3);
               String firstItem = individual_orders.get(i).get(4);
               String firstQuantity = individual_orders.get(i).get(5);

               String line = "+----------------------+------------------------------------------+";
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "Field", "Value");
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "OrderID", id);
               System.out.printf("| %-20s | %-40s |\n", "Order Timestamp", timestamp);
               System.out.printf("| %-20s | %-40s |\n", "Total Price", orderPrice);
               System.out.printf("| %-20s | %-40s |\n", "Order Status", status);
               System.out.printf("| %-20s | %-40s |\n", "", "");
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "Items - Quantity", firstItem + " x " + firstQuantity);
               for (int j = 6; j < individual_orders.get(i).size() - 1; j = j + 2) {
                  System.out.printf("| %-20s | %-40s |\n", "", individual_orders.get(i).get(j) + " x " +individual_orders.get(i).get(j + 1));
               }
               System.out.println(line);
            }
         }
         else { //if current user is a manager or driver
            String input_login; // get user login
            System.out.print("Input user login: ");
            input_login = in.readLine().trim();

            //check if user exists
            String check_user_query = String.format("SELECT * FROM Users WHERE login = '%s'", input_login);
            int count = esql.executeQuery(check_user_query);
            if (count <= 0) {
               System.out.println("User does not exist");
               return;
            }
            String food_order_query = 
               String.format("SELECT F.orderID, F.orderTimestamp, F.totalPrice, F.orderStatus, I.itemName, I.quantity FROM FoodOrder F JOIN ItemsInOrder I ON F.orderID = I.orderID GROUP BY F.orderID, I.itemName, I.quantity HAVING F.login = '%s'", 
               input_login);
            List<List<String>> user_orders = esql.executeQueryAndReturnResult(food_order_query);
            if (user_orders.size() <= 0) {
               System.out.println("No orders in history");
            }

            List<List<String>> individual_orders = new ArrayList<>();
            List<String> each_order = new ArrayList<>();
            each_order.add(user_orders.get(0).get(0));
            each_order.add(user_orders.get(0).get(1));
            each_order.add(user_orders.get(0).get(2));
            each_order.add(user_orders.get(0).get(3).trim());
            each_order.add(user_orders.get(0).get(4));
            each_order.add(user_orders.get(0).get(5));

            //grab items in orders order = (orderID, itemName, quantity, itemName, quanitity, ...)
            for (int i = 1; i < user_orders.size(); i++) {
               String orderID = user_orders.get(i).get(0);

               if (!orderID.equals(each_order.get(0))) {
                  individual_orders.add(each_order);
                  each_order = new ArrayList<>();
                  each_order.add(orderID);
                  each_order.add(user_orders.get(i).get(1));
                  each_order.add(user_orders.get(i).get(2));
                  each_order.add(user_orders.get(i).get(3).trim());
               }

               each_order.add(user_orders.get(i).get(4));
               each_order.add(user_orders.get(i).get(5));

            }
            individual_orders.add(each_order);

            //print out the orders
            for (int i = 0; i < individual_orders.size(); i++) {
               String id = individual_orders.get(i).get(0);
               String timestamp = individual_orders.get(i).get(1);
               String orderPrice = individual_orders.get(i).get(2);
               String status = individual_orders.get(i).get(3);
               String firstItem = individual_orders.get(i).get(4);
               String firstQuantity = individual_orders.get(i).get(5);

               String line = "+----------------------+------------------------------------------+";
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "Field", "Value");
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "OrderID", id);
               System.out.printf("| %-20s | %-40s |\n", "Order Timestamp", timestamp);
               System.out.printf("| %-20s | %-40s |\n", "Total Price", orderPrice);
               System.out.printf("| %-20s | %-40s |\n", "Order Status", status);
               System.out.printf("| %-20s | %-40s |\n", "", "");
               System.out.println(line);
               System.out.printf("| %-20s | %-40s |\n", "Items - Quantity", firstItem + " x " + firstQuantity);
               for (int j = 6; j < individual_orders.get(i).size() - 1; j = j + 2) {
                  System.out.printf("| %-20s | %-40s |\n", "", individual_orders.get(i).get(j) + " x " +individual_orders.get(i).get(j + 1));
               }
               System.out.println(line);
            }
         }

         return;
      }catch(Exception e) {
         System.err.println("An error occured when while viewing an order: " + e.getMessage());
      }
   }

   /*
      View your recent 5 orders 
   */
   public static void viewRecentOrders(PizzaStore esql) {
      try {
         String recent_order = String.format(
            "SELECT F.orderID, F.orderTimestamp, F.totalPrice, F.orderStatus, I.itemName, I.quantity FROM FoodOrder F JOIN ItemsInOrder I ON F.orderID = I.orderID GROUP BY F.orderID, I.itemName, I.quantity HAVING F.login = '%s' ORDER BY F.orderID DESC", 
            esql.getCurrentUser());
         List<List<String>> user_orders = esql.executeQueryAndReturnResult(recent_order);
         //turn the all user_orders into List of Lists of each order
         if (user_orders.size() <= 0) {
                  System.out.println("No orders in history");
         }
         List<List<String>> individual_orders = new ArrayList<>();
         List<String> each_order = new ArrayList<>();
         each_order.add(user_orders.get(0).get(0));
         each_order.add(user_orders.get(0).get(1));
         each_order.add(user_orders.get(0).get(2));
         each_order.add(user_orders.get(0).get(3).trim());
         each_order.add(user_orders.get(0).get(4));
         each_order.add(user_orders.get(0).get(5));

         //grab items in orders order = (orderID, itemName, quantity, itemName, quanitity, ...)
         for (int i = 1; i < user_orders.size(); i++) {
            String orderID = user_orders.get(i).get(0);

            if (!orderID.equals(each_order.get(0))) {
               individual_orders.add(each_order);
               each_order = new ArrayList<>();
               each_order.add(orderID);
               each_order.add(user_orders.get(i).get(1));
               each_order.add(user_orders.get(i).get(2));
               each_order.add(user_orders.get(i).get(3).trim());
            }

            each_order.add(user_orders.get(i).get(4));
            each_order.add(user_orders.get(i).get(5));
         }
         individual_orders.add(each_order);

         //print out the orders 
         for (int i = 0; i < Math.min(individual_orders.size(), 5); i++) {
            String id = individual_orders.get(i).get(0);
            String timestamp = individual_orders.get(i).get(1);
            String orderPrice = individual_orders.get(i).get(2);
            String status = individual_orders.get(i).get(3);
            String firstItem = individual_orders.get(i).get(4);
            String firstQuantity = individual_orders.get(i).get(5);

            String line = "+----------------------+------------------------------------------+";
            System.out.println(line);
            System.out.printf("| %-20s | %-40s |\n", "Field", "Value");
            System.out.println(line);
            System.out.printf("| %-20s | %-40s |\n", "OrderID", id);
            System.out.printf("| %-20s | %-40s |\n", "Order Timestamp", timestamp);
            System.out.printf("| %-20s | %-40s |\n", "Total Price", orderPrice);
            System.out.printf("| %-20s | %-40s |\n", "Order Status", status);
            System.out.printf("| %-20s | %-40s |\n", "", "");
            System.out.println(line);
            System.out.printf("| %-20s | %-40s |\n", "Items - Quantity", firstItem + " x " + firstQuantity);
            for (int j = 6; j < individual_orders.get(i).size() - 1; j = j + 2) {
               System.out.printf("| %-20s | %-40s |\n", "", individual_orders.get(i).get(j) + " x " +individual_orders.get(i).get(j + 1));
            }
            System.out.println(line);
         }
      }catch(Exception e) {
         System.err.println("An error occured when while viewing the 5 recent orders: " + e.getMessage());
      }
   }//end viewRecentOrders

   /*
      View your a specific order based on a provided orderID (customers can only view their own and managers/drivers can view any)
   */
   public static void viewOrderInfo(PizzaStore esql) {
      try {
         //Get role of current user
         String curr_user_login = esql.getCurrentUser();
         String user_role_query = String.format("SELECT role FROM Users WHERE login = '%s'", curr_user_login);
         List<List<String>> user = esql.executeQueryAndReturnResult(user_role_query);
         String user_role = user.get(0).get(0).trim();

         if (user_role.equals(String.format("customer"))) { //if customer, find order with user login
            String food_order_query = 
               String.format("SELECT F.orderID, F.orderTimestamp, F.totalPrice, F.orderStatus, I.itemName, I.quantity FROM FoodOrder F JOIN ItemsInOrder I ON F.orderID = I.orderID GROUP BY F.orderID, I.itemName, I.quantity HAVING F.login = '%s' ORDER BY F.orderID ASC", 
               curr_user_login);
            List<List<String>> user_orders = esql.executeQueryAndReturnResult(food_order_query);
            if (user_orders.size() <= 0) {
               System.out.println("No orders in history");
               return;
            }

            //turn the all user_orders into List of Lists of each order
            List<List<String>> individual_orders = new ArrayList<>();
            List<String> each_order = new ArrayList<>();
            each_order.add(user_orders.get(0).get(0));
            each_order.add(user_orders.get(0).get(1));
            each_order.add(user_orders.get(0).get(2));
            each_order.add(user_orders.get(0).get(3).trim());
            each_order.add(user_orders.get(0).get(4));
            each_order.add(user_orders.get(0).get(5));

            //grab items in orders order = (orderID, itemName, quantity, itemName, quanitity, ...)
            for (int i = 1; i < user_orders.size(); i++) {
               String orderID = user_orders.get(i).get(0);

               if (!orderID.equals(each_order.get(0))) {
                  individual_orders.add(each_order);
                  each_order = new ArrayList<>();
                  each_order.add(orderID);
                  each_order.add(user_orders.get(i).get(1));
                  each_order.add(user_orders.get(i).get(2));
                  each_order.add(user_orders.get(i).get(3).trim());
               }

               each_order.add(user_orders.get(i).get(4));
               each_order.add(user_orders.get(i).get(5));

            }
            individual_orders.add(each_order);
            
            String order_id;
            System.out.print("Provide the orderID of the order you want to look at: ");
            order_id = in.readLine().trim();

            //print out the orders 
            for (int i = 0; i < individual_orders.size(); i++) {
               String id = individual_orders.get(i).get(0);
               String timestamp = individual_orders.get(i).get(1);
               String orderPrice = individual_orders.get(i).get(2);
               String status = individual_orders.get(i).get(3);
               String firstItem = individual_orders.get(i).get(4);
               String firstQuantity = individual_orders.get(i).get(5);

               if (id.equals(order_id)) {
                  String line = "+----------------------+------------------------------------------+";
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "Field", "Value");
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "OrderID", id);
                  System.out.printf("| %-20s | %-40s |\n", "Order Timestamp", timestamp);
                  System.out.printf("| %-20s | %-40s |\n", "Total Price", orderPrice);
                  System.out.printf("| %-20s | %-40s |\n", "Order Status", status);
                  System.out.printf("| %-20s | %-40s |\n", "", "");
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "Items - Quantity", firstItem + " x " + firstQuantity);
                  for (int j = 6; j < individual_orders.get(i).size() - 1; j = j + 2) {
                     System.out.printf("| %-20s | %-40s |\n", "", individual_orders.get(i).get(j) + " x " +individual_orders.get(i).get(j + 1));
                  }
                  System.out.println(line);
               }
            }
         }
         else { //if current user is a manager or driver
            String food_order_query = 
               String.format("SELECT F.orderID, F.orderTimestamp, F.totalPrice, F.orderStatus, I.itemName, I.quantity FROM FoodOrder F JOIN ItemsInOrder I ON F.orderID = I.orderID GROUP BY F.orderID, I.itemName, I.quantity ORDER BY F.orderID ASC");
            List<List<String>> user_orders = esql.executeQueryAndReturnResult(food_order_query);
            if (user_orders.size() <= 0) {
               System.out.println("No orders in history");
               return;
            }

            List<List<String>> individual_orders = new ArrayList<>();
            List<String> each_order = new ArrayList<>();
            each_order.add(user_orders.get(0).get(0));
            each_order.add(user_orders.get(0).get(1));
            each_order.add(user_orders.get(0).get(2));
            each_order.add(user_orders.get(0).get(3).trim());
            each_order.add(user_orders.get(0).get(4));
            each_order.add(user_orders.get(0).get(5));

            //grab items in orders order = (orderID, itemName, quantity, itemName, quanitity, ...)
            for (int i = 1; i < user_orders.size(); i++) {
               String orderID = user_orders.get(i).get(0);

               if (!orderID.equals(each_order.get(0))) {
                  individual_orders.add(each_order);
                  each_order = new ArrayList<>();
                  each_order.add(orderID);
                  each_order.add(user_orders.get(i).get(1));
                  each_order.add(user_orders.get(i).get(2));
                  each_order.add(user_orders.get(i).get(3).trim());
               }

               each_order.add(user_orders.get(i).get(4));
               each_order.add(user_orders.get(i).get(5));

            }
            individual_orders.add(each_order);

            String order_id; //get orderid
            System.out.print("Provide the orderID of the order you want to look at: ");
            order_id = in.readLine().trim();

            //print out the orders
            for (int i = 0; i < individual_orders.size(); i++) {
               String id = individual_orders.get(i).get(0);
               String timestamp = individual_orders.get(i).get(1);
               String orderPrice = individual_orders.get(i).get(2);
               String status = individual_orders.get(i).get(3);
               String firstItem = individual_orders.get(i).get(4);
               String firstQuantity = individual_orders.get(i).get(5);

               if (id.equals(order_id)) {
                  String line = "+----------------------+------------------------------------------+";
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "Field", "Value");
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "OrderID", id);
                  System.out.printf("| %-20s | %-40s |\n", "Order Timestamp", timestamp);
                  System.out.printf("| %-20s | %-40s |\n", "Total Price", orderPrice);
                  System.out.printf("| %-20s | %-40s |\n", "Order Status", status);
                  System.out.printf("| %-20s | %-40s |\n", "", "");
                  System.out.println(line);
                  System.out.printf("| %-20s | %-40s |\n", "Items - Quantity", firstItem + " x " + firstQuantity);
                  for (int j = 6; j < individual_orders.get(i).size() - 1; j = j + 2) {
                     System.out.printf("| %-20s | %-40s |\n", "", individual_orders.get(i).get(j) + " x " +individual_orders.get(i).get(j + 1));
                  }
                  System.out.println(line);
               }
            }
         }

         return;
      }catch(Exception e) {
         System.err.println("An error occured when while viewing an order: " + e.getMessage());
      }
   }//end viewOrderInfo

   /**
    * Customers should be able to view the list of all stores. They should see all
    * information about the location of the store, the storeID, the review score, and whether or
    * not it is open
    */
    public static void viewStores(PizzaStore esql) {
      try {
         // Query to fetch all necessary store details
         String query = "SELECT storeID, address, city, state, reviewScore, isOpen FROM Store ORDER BY storeID;";
         List<List<String>> stores = esql.executeQueryAndReturnResult(query);
         
         if (stores.size() == 0) {
            System.out.println("No stores available at the moment.");
            return;
         }
         
         // Define a formatted table header
         String line = "+---------+-----------------------------+--------------------+----------------------+-------------+---------+";
         System.out.println(line);
         System.out.printf("| %-7s | %-27s | %-18s | %-20s | %-11s | %-7s |\n", 
                           "StoreID", "Address", "City", "State", "ReviewScore", "IsOpen");
         System.out.println(line);
         
         // Loop through the result set and print each store record
         for (List<String> store : stores) {
            String storeID = store.get(0);
            String address = store.get(1);
            String city = store.get(2);
            String state = store.get(3);
            String reviewScoreStr = store.get(4);
            String isOpen = store.get(5);

            // Convert review score from string to double
            double reviewScore = Double.parseDouble(reviewScoreStr);
            // Assume review score is out of 5.
            int fullStars = (int) reviewScore;
            int emptyStars = 5 - fullStars;
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < fullStars; i++) {
               stars.append("★");
            }
            for (int i = 0; i < emptyStars; i++) {
               stars.append("☆");
            }
            
            System.out.printf("| %-7s | %-27s | %-18s | %-20s | %-11s | %-7s |\n", 
                              storeID, address, city, state, stars.toString(), isOpen);
         }
         System.out.println(line);
      } catch (Exception e) {
         System.err.println("An error occurred while viewing stores: " + e.getMessage());
      }
   }

   /**
    * Update the Order Status of a given orderID (must be driver or manager)
   */
   public static void updateOrderStatus(PizzaStore esql) {
      try {
         String curr_user_login = esql.getCurrentUser();
         String user_role_query = String.format("SELECT role FROM Users WHERE login = '%s'", curr_user_login);
         List<List<String>> user = esql.executeQueryAndReturnResult(user_role_query);
         String user_role = user.get(0).get(0).trim(); // check if role is not customer

         if (user_role.equals(String.format("driver")) || user_role.equals(String.format("manager"))) {
            String update_orderid; //get order id to update
            System.out.print("Order ID: ");
            update_orderid = in.readLine().trim();

            String check_order = String.format("SELECT * FROM FoodOrder WHERE orderID = '%s'", update_orderid);
            int count = esql.executeQuery(check_order); //check if order exist
            if (count <= 0) {
               System.out.println("Error: Order does not exist.\n");
               return;
            }

            System.out.println(String.format("Update Status for Order %s: ", update_orderid));
            System.out.println("1. Complete");
            System.out.println("2. Incomplete");

            String update_status_query;
            String update_status = "";
            switch (readChoice()) {
               case 1:
                  update_status_query = String.format("UPDATE FoodOrder SET orderStatus = 'complete' WHERE orderId = '%s'", update_orderid);
                  esql.executeUpdate(update_status_query);
                  update_status = "complete";
                  break;
               case 2:
                  update_status_query = String.format("UPDATE FoodOrder SET orderStatus = 'incomplete' WHERE orderId = '%s'", update_orderid);
                  esql.executeUpdate(update_status_query);
                  update_status = "incomplete";
                  break;
               default:
                  break;
            }

            System.out.println("Order status for order " + update_orderid + " updated successfully to " + update_status + ".\n"); 

            return; 
         }
      }catch(Exception e) {
            System.err.println("An error occured when while updating the order status: " + e.getMessage());   
      }
   }// end updateOrderStatus

   /*
      Managers can update the information of any item in the menu given the itemName and add in new items.
   */
   public static void updateMenu(PizzaStore esql) {
      try {
         String curr_user_login = esql.getCurrentUser();
         String user_role_query = String.format("SELECT role FROM Users WHERE login = '%s'", curr_user_login);
         List<List<String>> user = esql.executeQueryAndReturnResult(user_role_query);
         String user_role = user.get(0).get(0).trim();

         if (user_role.equals(String.format("manager"))) {
            String menu_query = String.format("SELECT itemName, price, typeOfItem FROM Items ORDER BY CASE WHEN typeOfItem = ' entree' THEN 1 WHEN typeOfItem = ' drinks' THEN 2 WHEN typeOfItem = ' sides' THEN 3 END ASC");
            List<List<String>> menu = esql.executeQueryAndReturnResult(menu_query);//ORDER BY price DESC
            System.out.println(menu);
            String line = "+------------------------------------------+----------------------+"; //rbandyo,cpendreigho
            System.out.println(line);
            System.out.printf("| %-40s | %-20s |\n", "Item", "Price");
            System.out.println(line);
            for (int i = 0; i < menu.size(); ++i) {
               String item = menu.get(i).get(0);
               String price = menu.get(i).get(1);
               System.out.printf("| %-40s | %-20s |\n", item, price);
            }
            System.out.println(line);
            System.out.println("MENU UPDATE");
            System.out.println("-----------");
            System.out.println("1. Update an existing item");
            System.out.println("2. Add a new item");
            System.out.println("9. < EXIT");
            switch(readChoice()) {//rbandyo,cpendreigho
               case 1:
                  String update_item;
                  System.out.print("Select an item to update: ");
                  update_item = in.readLine().trim();
                  String check_item = String.format("SELECT * FROM Items WHERE itemName = '%s'", update_item);
                  int count = esql.executeQuery(check_item);
                  if (count <= 0) {
                     System.out.println(String.format("%s is not on the menu", update_item));
                     break;
                  }
                  System.out.println("ITEM UPDATE");
                  System.out.println("-----------");
                  System.out.println("1. Update item name");
                  System.out.println("2. Update item ingredients");
                  System.out.println("3. Update item type");
                  System.out.println("4. Update item price");
                  System.out.println("5. Update item description");
                  System.out.println("9. < EXIT");
                  switch(readChoice()) {
                     case 1:
                        String new_item_name;
                        System.out.print(String.format("Provide a new name for %s: ", update_item));
                        new_item_name = in.readLine().trim();
                        String check_item2 = String.format("SELECT * FROM Items WHERE itemName = '%s'", new_item_name);
                        int count2 = esql.executeQuery(check_item2);
                        if (count2 > 0) {
                           System.out.println(String.format("%s is already on the menu", new_item_name));
                           break;
                        }
                        String update_name = String.format("UPDATE Items SET itemName = '%s' WHERE itemName = '%s'", new_item_name, update_item);
                        esql.executeUpdate(update_name);
                        System.out.println(String.format("%s is now %s", update_item, new_item_name));
                        break;
                     case 2:
                        String new_item_ingredients;
                        List<List<String>> curr_ingredients = esql.executeQueryAndReturnResult(String.format("SELECT ingredients FROM Items WHERE itemName = '%s'", update_item));
                        System.out.println(String.format("Current ingredients for %s: %s", update_item, curr_ingredients.get(0).get(0)));
                        System.out.print(String.format("Provide updated ingredients for %s (separated by commas): ", update_item));
                        new_item_ingredients = in.readLine().trim();
                        new_item_ingredients = String.format("\"%s\"", new_item_ingredients);
                        String update_ingredients = String.format("UPDATE Items SET ingredients = '%s' WHERE itemName = '%s'", new_item_ingredients, update_item);
                        esql.executeUpdate(update_ingredients);
                        System.out.println(String.format("%s's is now %s", update_item, new_item_ingredients));
                        break;
                     case 3:
                        List<List<String>> curr_type = esql.executeQueryAndReturnResult(String.format("SELECT typeOfItem FROM Items WHERE itemName = '%s'", update_item));
                        String update_type = String.format("UPDATE Items SET typeOfItem = ");
                        String new_type = "";
                        System.out.println(String.format("UPDATE %s FROM %s TO: ", update_item, curr_type.get(0).get(0).trim()));
                        System.out.println("-----------");
                        System.out.println("1. entree");
                        System.out.println("2. drinks");
                        System.out.println("3. sides");
                        switch(readChoice()) {
                           case 1:
                              new_type = " entree";
                              break;
                           case 2:
                              new_type = " drinks";
                              break;
                           case 3:
                              new_type = " sides";
                              break;
                           default:
                              System.out.println("Invalid choice");
                              return;
                        }
                        update_type += String.format("'%s' WHERE itemName = '%s'", new_type, update_item);
                        esql.executeUpdate(update_type);
                        break;
                     case 4:
                        double new_item_price = 0.0;
                        System.out.print(String.format("Provide a new price for %s", update_item));
                        try {
                           new_item_price = Double.parseDouble(in.readLine().trim());
                        }catch(Exception e) {
                           System.err.println("Invalid input: " + e.getMessage());
                        }
                        String update_price = String.format("UPDATE Items SET price = '%f' WHERE itemName = '%s'", new_item_price, update_item);
                        esql.executeUpdate(update_price);
                        System.out.println(String.format("%s is now %f", update_item, new_item_price));
                        break;
                     case 5:
                        String new_item_description;
                        List<List<String>> curr_description = esql.executeQueryAndReturnResult(String.format("SELECT description FROM Items WHERE itemName = '%s'", update_item));
                        System.out.println(String.format("Current description for %s: %s", update_item, curr_description.get(0).get(0)));
                        System.out.print(String.format("Provide updated decription for %s : ", update_item));
                        new_item_description = in.readLine().trim();
                        new_item_description = String.format("\"%s\"", new_item_description);
                        String update_description = String.format("UPDATE Items SET description = '%s' WHERE itemName = '%s'", new_item_description, update_item);
                        esql.executeUpdate(update_description);
                        break;
                     case 9:
                        break;
                     default:
                        System.out.println("Invalid choice");
                        break;
                  }
                  break;
               case 2:
                  String new_item_name;
                  String new_item_ingredients;
                  String new_item_type;
                  double new_item_price = 0.0;
                  String new_item_description;
                  
                  System.out.print("Provide the new item's name: ");
                  new_item_name = in.readLine().trim();
                  int count3 = esql.executeQuery(String.format("SELECT * FROM Items WHERE itemName = '%s'", new_item_name));
                  if (count3 > 0) {
                     System.out.println("Item is already on the menu");
                     return;
                  }
                  System.out.print("Provide the new item's ingredients (separated by commas): ");
                  new_item_ingredients = in.readLine().trim();
                  new_item_ingredients = String.format("\"%s\"");

                  System.out.println("Provide the new item's type: ");
                  System.out.println("1. entree");
                  System.out.println("2. drinks");
                  System.out.println("3. sides");
                  switch(readChoice()) {
                     case 1:
                        new_item_type = " entree";
                        break;
                     case 2:
                        new_item_type = " drinks";
                        break;
                     case 3:
                        new_item_type = " sides";
                        break;
                     default:
                        System.out.println("Invalid choice");
                        return;
                  }

                  System.out.print("Provide the new item's price: ");
                  try {
                     new_item_price = Double.parseDouble(in.readLine().trim());
                  }catch(Exception e) {
                     System.err.println("Invalid input: " + e.getMessage());
                  }

                  System.out.print("Provide a description of the new item: ");
                  new_item_description = in.readLine().trim();
                  new_item_description = String.format("\"%s\"", new_item_description);

                  String insert_new_item = String.format(
                     "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %f, '%s')",
                     new_item_name, new_item_ingredients, new_item_type, new_item_price, new_item_description);
                  esql.executeUpdate(insert_new_item);
                  System.out.println(String.format("%s is now on the menu", new_item_name));
                  break;
               case 9:
                  break;
               default:
                  System.out.println("Invalid choice");
                  break;
            }
            System.out.println("UPDATED MENU");
            menu_query = String.format("SELECT itemName, price, typeOfItem FROM Items ORDER BY CASE WHEN typeOfItem = ' entree' THEN 1 WHEN typeOfItem = ' drinks' THEN 2 WHEN typeOfItem = ' sides' THEN 3 END ASC");
            menu = esql.executeQueryAndReturnResult(menu_query);//ORDER BY price DESC
            System.out.println(menu);
            System.out.println(line);
            System.out.printf("| %-40s | %-20s |\n", "Item", "Price");
            System.out.println(line);
            for (int i = 0; i < menu.size(); ++i) {
               String item = menu.get(i).get(0);
               String price = menu.get(i).get(1);
               System.out.printf("| %-40s | %-20s |\n", item, price);
            }
            return;
         }
         else {
            System.out.println("You are not a manager");
         }

         return;
      }catch(Exception e) {
         System.err.println("An error occured when updating the menu: " + e.getMessage());
      }
   }//end updateMenu
   
   /**
    * Update a user's login and role as a manager
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

