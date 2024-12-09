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
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

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
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



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
   public static void CreateUser(PizzaStore esql) {
      String login;
      String password;
      String role = "customer";
      String favoriteItems;
      String phoneNum;

      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      System.out.print("Thank you for creating a User at Papa's Pizzeria! What do you want your Username to be?\n");
      do {
         System.out.print("Username: ");
         try {
               login = in.readLine();
               if (login.trim().isEmpty() || login.length() > 50) {
                  System.out.println("Woah there! Is there any way you can create a different username? Please try again.\n");
               } else {
                  break;
               }
         } catch (IOException e) {
               System.out.println("An error occurred while reading your input. Please try again.");
         }
      } while (true);

      System.out.print("Cool name! What do you want your password to be?\n");
      do {
         System.out.print("Password: ");
         try {
               password = in.readLine();
               if (password.trim().isEmpty() || password.length() > 30) {
                  System.out.println("Sorry, that password is invalid. Please try again!\n");
               } else {
                  break;
               }
         } catch (IOException e) {
               System.out.println("An error occurred while reading your input. Please try again.");
         }
      } while (true);

      System.out.print("That's a very safe password! Now we would like to know ourselves... What's your favorite item on our menu?\n");
      do {
         System.out.print("Favorite Item: ");
         try {
               favoriteItems = in.readLine();
               break;
         } catch (IOException e) {
               System.out.println("An error occurred while reading your input. Please try again.");
         }
      } while (true);

      System.out.print("Lastly, all we need is your phone number to send you discounts and deals!\n");
      do {
         System.out.print("Phone Number: ");
         try {
               phoneNum = in.readLine();
               if (phoneNum.trim().isEmpty() || phoneNum.length() > 20) {
                  System.out.println("That doesn't seem right. Please try again.\n");
               } else {
                  break;
               }
         } catch (IOException e) {
               System.out.println("An error occurred while reading your input. Please try again.");
         }
      } while (true);

      try {
         String query = "INSERT INTO Users (login, password, role, favoriteItems, phoneNum) VALUES ('" + login + "', '" + password + "', '" + role + "', '" + favoriteItems + "', '" + phoneNum + "');";
         esql.executeUpdate(query);
         System.out.println("User created successfully!");
      } catch (Exception e) {
         System.err.println("Error inserting user into database: " + e.getMessage());
      }
   }



   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try {
            System.out.print("Enter Username: ");
            String login = in.readLine();

            System.out.print("Enter Password: ");
            String password = in.readLine();

            String query = String.format("SELECT * FROM Users WHERE login='%s' AND password='%s'", login, password);
            int userCount = esql.executeQuery(query);

            if (userCount > 0) {
               System.out.println("Login successful!");
               return login; // Return the logged-in user's login
            } 
            else {
               System.out.println("Invalid login credentials.");
               return null;
            }

         }catch (Exception e) {
               System.err.println(e.getMessage());
               return null;
            }
            
   }

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String authorisedUser) {
      try {
         // Use authorisedUser directly in the query
         String query = String.format("SELECT * FROM Users WHERE login='%s'", authorisedUser);

         // Execute the query and get the result
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         // Check if the user's profile exists
         if (result.isEmpty()) {
            System.out.println("No user profile found for the current login.");
         } else {
            // Display all user details (username, role, etc.)
            System.out.println("User Profile:");
            System.out.println("Login: " + result.get(0).get(0)); // Username
            System.out.println("Password: " + result.get(0).get(1)); // Password
            System.out.println("Role: " + result.get(0).get(2)); // Role
            System.out.println("Favorite Items: " + result.get(0).get(3)); // Favorite Items
            System.out.println("Phone Number: " + result.get(0).get(4)); // Phone Number
         }
      } catch (Exception e) {
         System.err.println("An error occurred: " + e.getMessage());
      }
   }

   
   public static void updateProfile(PizzaStore esql, String authorisedUser) {
      try {
         // Fetch the current profile information to show the user
         String query = String.format("SELECT * FROM Users WHERE login='%s';", authorisedUser);
         List<List<String>> currentDetails = esql.executeQueryAndReturnResult(query);
   
         // Display current profile details
         if (!currentDetails.isEmpty()) {
            System.out.println("Current Profile Details:");
            System.out.println("Password: " + currentDetails.get(0).get(1));
            System.out.println("Phone Number: " + currentDetails.get(0).get(4));
            System.out.println("Favorite Items: " + currentDetails.get(0).get(3));
         } else {
            System.out.println("No profile found for the user.");
            return;
         }
   
         // Prompt user for new values
         System.out.print("Enter new password (Leave blank to keep the same): ");
         String newPassword = in.readLine();
   
         System.out.print("Enter new phone number (Leave blank to keep the same): ");
         String newPhoneNum = in.readLine();
   
         System.out.print("Enter new favorite items (Leave blank to keep the same): ");
         String newFavoriteItems = in.readLine();
   
         // Construct the SQL update query
         String updateQuery = "UPDATE Users SET ";
         boolean first = true;
   
         if (newPassword != null && !newPassword.isEmpty()) {
            updateQuery += "password = '" + newPassword + "'";
            first = false;
         }
   
         if (newPhoneNum != null && !newPhoneNum.isEmpty()) {
            if (!first) updateQuery += ", ";
            updateQuery += "phoneNum = '" + newPhoneNum + "'";
            first = false;
         }
   
         if (newFavoriteItems != null && !newFavoriteItems.isEmpty()) {
            if (!first) updateQuery += ", ";
            updateQuery += "favoriteItems = '" + newFavoriteItems + "'";
         }
   
         updateQuery += String.format(" WHERE login='%s';", authorisedUser);
   
         // Execute the query
         esql.executeUpdate(updateQuery);
         System.out.println("Profile updated successfully!");
   
      } catch (Exception e) {
         System.err.println("Error updating profile: " + e.getMessage());
      }
   }
   

   public static void viewMenu(PizzaStore esql) {
      try {
         String query = "SELECT itemName, ingredients, typeOfItem, price, description FROM Items;";
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         if (results.isEmpty()) {
            System.out.println("The menu is empty. No items available.");
            return;
         }
   
         System.out.println("---- Menu ----");
         for (List<String> item : results) {
            System.out.println("Item Name: " + item.get(0));
            System.out.println("Ingredients: " + item.get(1));
            System.out.println("Type: " + item.get(2));
            System.out.println("Price: $" + item.get(3));
            System.out.println("Description: " + item.get(4));
            System.out.println("-----------------------");
         }
   
      } catch (Exception e) {
         System.err.println("Error displaying the menu: " + e.getMessage());
      }
   }   


   public static void placeOrder(PizzaStore esql) {}
   public static void viewAllOrders(PizzaStore esql) {}
   public static void viewRecentOrders(PizzaStore esql) {}
   public static void viewOrderInfo(PizzaStore esql) {}


   public static void viewStores(PizzaStore esql) {
      try {
         // Step 1: Query the Store table to get all the store details
         String query = "SELECT storeID, address, city, state, isOpen, reviewScore FROM Store;";
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         // Step 2: Check if there are any stores
         if (results.isEmpty()) {
            System.out.println("No stores found in the database.");
            return;
         }
   
         // Step 3: Display the store details
         System.out.println("---- Store List ----");
         for (List<String> store : results) {
            String storeID = store.get(0);
            String address = store.get(1);
            String city = store.get(2);
            String state = store.get(3);
            String isOpen = store.get(4);
            String reviewScore = store.get(5) == null ? "N/A" : store.get(5);
   
            System.out.println("Store ID: " + storeID);
            System.out.println("Address: " + address);
            System.out.println("City: " + city);
            System.out.println("State: " + state);
            System.out.println("Is Open: " + isOpen);
            System.out.println("Review Score: " + reviewScore);
            System.out.println("-----------------------");
         }
      } catch (Exception e) {
         System.err.println("Error displaying stores: " + e.getMessage());
      }
   }

   public static void updateOrderStatus(PizzaStore esql, String login) {
      // Use a single Scanner object for the whole program if tied to System.in
      Scanner scanner = new Scanner(System.in);  // Create Scanner object for input
  
      try {
          // Query to check the user's role
          String roleQuery = "SELECT role FROM Users WHERE login = '" + login + "';";
          List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
  
          if (roleResult.isEmpty()) {
              System.out.println("Invalid login.");
              return;  // Exit method cleanly
          }
  
          // Get the role of the user
          String role = roleResult.get(0).get(0).trim();
  
          if (!"manager".equalsIgnoreCase(role) && !"driver".equalsIgnoreCase(role)) {
              System.out.println("Access denied. Only managers and drivers can update orders.");
              return;  // Exit method cleanly
          }
  
          // Get the order ID and the new status
          System.out.print("Enter the Order ID to update: ");
          int orderID = Integer.parseInt(scanner.nextLine().trim());
  
          System.out.print("Enter the new status for this order: ");
          String newStatus = scanner.nextLine().trim();
  
          // Form the update query to change the order status
          String updateQuery = "UPDATE FoodOrder SET orderStatus = '" + newStatus.replace("'", "''") + "' WHERE orderID = " + orderID + ";";
  
          // Execute the update query
          esql.executeUpdate(updateQuery);
  
          // Provide feedback to the user
          System.out.println("Order status updated successfully.");
      } catch (NumberFormatException e) {
          System.out.println("Invalid input. Order ID must be a number.");
      } catch (Exception e) {
          System.out.println("Error: " + e.getMessage());
      }
  }
  

   public static void updateMenu(PizzaStore esql, String authorisedUser) {
      try {
         // Step 1: Check if the authorisedUser is a manager
         String roleQuery = String.format("SELECT role FROM Users WHERE login='%s';", authorisedUser);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
   
         if (roleResult.isEmpty() || !"manager".equalsIgnoreCase(roleResult.get(0).get(0).trim())) {
            System.out.println("Access denied. Only managers can update the menu.");
            return;
         }
   
         // Step 2: Display the current menu for reference
         String menuQuery = "SELECT itemName, ingredients, typeOfItem, price, description FROM Items;";
         List<List<String>> menuItems = esql.executeQueryAndReturnResult(menuQuery);
   
         System.out.println("---- Current Menu ----");
         for (List<String> item : menuItems) {
            System.out.println("Item Name: " + item.get(0));
            System.out.println("Ingredients: " + item.get(1));
            System.out.println("Type: " + item.get(2));
            System.out.println("Price: $" + item.get(3));
            System.out.println("Description: " + item.get(4));
            System.out.println("-----------------------");
         }
   
         // Step 3: Allow manager to update menu items
         System.out.println("Do you want to update an existing item or add a new item?");
         System.out.println("1. Update an existing item");
         System.out.println("2. Add a new item");
         int choice = readChoice();
   
         if (choice == 1) {
            // Update an existing menu item
            System.out.print("Enter the name of the item you want to update: ");
            String itemName = in.readLine().trim();
   
            // Validate if the item exists
            String validateQuery = String.format("SELECT * FROM Items WHERE itemName = '%s';", itemName.replace("'", "''"));
            int itemCount = esql.executeQuery(validateQuery);
   
            if (itemCount == 0) {
               System.out.println("No item found with the provided name.");
               return;
            }
   
            System.out.print("Enter the new ingredients (Leave blank to keep the same): ");
            String newIngredients = in.readLine().trim().replace("'", "''");
   
            System.out.print("Enter the new type of item (Leave blank to keep the same): ");
            String newType = in.readLine().trim().replace("'", "''");
   
            System.out.print("Enter the new price (Leave blank to keep the same): ");
            String newPrice = in.readLine().trim();
   
            System.out.print("Enter the new description (Leave blank to keep the same): ");
            String newDescription = in.readLine().trim().replace("'", "''");
   
            StringBuilder updateQuery = new StringBuilder("UPDATE Items SET ");
            boolean changed = false;
   
            if (!newIngredients.isEmpty()) {
               updateQuery.append("ingredients = '").append(newIngredients).append("'");
               changed = true;
            }
   
            if (!newType.isEmpty()) {
               if (changed) updateQuery.append(", ");
               updateQuery.append("typeOfItem = '").append(newType).append("'");
               changed = true;
            }
   
            if (!newPrice.isEmpty()) {
               if (changed) updateQuery.append(", ");
               updateQuery.append("price = ").append(newPrice);
               changed = true;
            }
   
            if (!newDescription.isEmpty()) {
               if (changed) updateQuery.append(", ");
               updateQuery.append("description = '").append(newDescription).append("'");
               changed = true;
            }
   
            if (!changed) {
               System.out.println("No updates were made. All fields were left blank.");
               return;
            }
   
            updateQuery.append(" WHERE itemName = '").append(itemName.replace("'", "''")).append("';");
   
            esql.executeUpdate(updateQuery.toString());
            System.out.println("Item updated successfully!");
         } else if (choice == 2) {
            // Add a new menu item
            System.out.print("Enter the name of the new item: ");
            String name = in.readLine().trim().replace("'", "''");
   
            System.out.print("Enter the ingredients of the new item: ");
            String ingredients = in.readLine().trim().replace("'", "''");
   
            System.out.print("Enter the type of the new item: ");
            String type = in.readLine().trim().replace("'", "''");
   
            System.out.print("Enter the price of the new item: ");
            String price = in.readLine().trim();
   
            System.out.print("Enter the description of the new item: ");
            String description = in.readLine().trim().replace("'", "''");
   
            if (name.isEmpty() || ingredients.isEmpty() || type.isEmpty() || price.isEmpty() || description.isEmpty()) {
               System.out.println("All fields are required to add a new item.");
               return;
            }
   
            String insertQuery = String.format(
               "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %s, '%s');",
               name, ingredients, type, price, description
            );
   
            esql.executeUpdate(insertQuery);
            System.out.println("New item added successfully!");
         } else {
            System.out.println("Invalid choice. Returning to menu.");
         }
   
      } catch (Exception e) {
         System.err.println("Error updating menu: " + e.getMessage());
         e.printStackTrace();
      }
   }

   public static void updateUser(PizzaStore esql, String authorisedUser) {
      try {
         // Step 1: Check if the authorisedUser is a manager
         String roleQuery = String.format("SELECT role FROM Users WHERE login='%s';", authorisedUser);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty() || !"manager".equalsIgnoreCase(roleResult.get(0).get(0).trim())) {
            System.out.println("Access denied. Only managers can update user details.");
            return;
         }

         // Step 2: Allow manager to select a user to update
         System.out.print("Enter the username of the user you want to update: ");
         String targetUser = in.readLine().trim();

         // Validate if the user exists
         String validateQuery = String.format("SELECT * FROM Users WHERE login = '%s';", targetUser.replace("'", "''"));
         int userCount = esql.executeQuery(validateQuery);

         if (userCount == 0) {
            System.out.println("No user found with the provided username.");
            return;
         }

         // Step 3: Prompt for updates
         System.out.print("Enter the new password (Leave blank to keep the same): ");
         String newPassword = in.readLine().trim().replace("'", "''");

         System.out.print("Enter the new role (Leave blank to keep the same): ");
         String newRole = in.readLine().trim().replace("'", "''");

         System.out.print("Enter the new favorite items (Leave blank to keep the same): ");
         String newFavoriteItems = in.readLine().trim().replace("'", "''");

         System.out.print("Enter the new phone number (Leave blank to keep the same): ");
         String newPhoneNum = in.readLine().trim().replace("'", "''");

         // Step 4: Construct the update query
         StringBuilder updateQuery = new StringBuilder("UPDATE Users SET ");
         boolean changed = false;

         if (!newPassword.isEmpty()) {
            updateQuery.append("password = '").append(newPassword).append("'");
            changed = true;
         }

         if (!newRole.isEmpty()) {
            if (changed) updateQuery.append(", ");
            updateQuery.append("role = '").append(newRole).append("'");
            changed = true;
         }

         if (!newFavoriteItems.isEmpty()) {
            if (changed) updateQuery.append(", ");
            updateQuery.append("favoriteItems = '").append(newFavoriteItems).append("'");
            changed = true;
         }

         if (!newPhoneNum.isEmpty()) {
            if (changed) updateQuery.append(", ");
            updateQuery.append("phoneNum = '").append(newPhoneNum).append("'");
            changed = true;
         }

         if (!changed) {
            System.out.println("No updates were made. All fields were left blank.");
            return;
         }

         updateQuery.append(" WHERE login = '").append(targetUser.replace("'", "''")).append("';");

         // Step 5: Execute the update query
         esql.executeUpdate(updateQuery.toString());
         System.out.println("User updated successfully!");

      } catch (Exception e) {
         System.err.println("Error updating user: " + e.getMessage());
         e.printStackTrace();
      }
   }

}//end Papa's Pizzaria

