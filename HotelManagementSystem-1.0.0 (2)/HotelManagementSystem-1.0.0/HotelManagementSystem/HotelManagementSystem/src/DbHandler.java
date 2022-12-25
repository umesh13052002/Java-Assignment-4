import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static java.lang.System.out;

public class DbHandler {

    private static List<String> tokens = new ArrayList<String>();
    static Connection connection_menu = null;
    static Connection connection_order = null;
    static Connection connection_feedback = null;

    static Connection connection_auth = null;
    static String jdbcURL_menu = "jdbc:sqlite:C:\\HotelData\\menu.sqlite";
    static String jdbcURL_order = "jdbc:sqlite:C:\\HotelData\\orders.sqlite";

    static String getJdbcURL_auth = "jdbc:sqlite:C:\\HotelData\\auth.sqlite";
    static String jdbcURL_feedback = "jdbc:sqlite:C:\\HotelData\\feedback.sqlite";

    public void printTokens()
    {
        for(String tok : tokens)
        {
            out.println(tok);
        }
    }
    public static List<String> getToken() throws SQLException {
        List<String> tokens = new ArrayList<String>();
        String query = "SELECT * FROM accounts;";
        Statement s = connection_auth.createStatement();
        ResultSet set = s.executeQuery(query);
        
        while(set.next())
        {
            tokens.add(set.getString("password"));
        }
        return tokens;
    }
    public static boolean validator(String token)
    {
          for(String tok: tokens)
            if(tok.equals(token)) {
                return true;
            }

        return false;
    }

    public static void connect()
    {
        try {
            connection_menu = DriverManager.getConnection(jdbcURL_menu);
            connection_order = DriverManager.getConnection(jdbcURL_order);
            connection_feedback = DriverManager.getConnection(jdbcURL_feedback);
            connection_auth = DriverManager.getConnection(getJdbcURL_auth);

            //Check if user exists
            String query = "SELECT * FROM accounts;";
            Statement s = connection_auth.createStatement();
            ResultSet set = s.executeQuery(query);
            //Check if table is empty
            if(!set.next())
            {
                out.println("Creating management console account...");
                //Start account setup
                createNewAccountFirstTime();
            }
              tokens = getToken();
        } catch (SQLException e) {
            out.println("Error connecting");
            e.printStackTrace();
        }
    }

    private static void createNewAccountFirstTime() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        out.println("Username: ");
        String username = scanner.nextLine();
        out.println("Password: ");
        String password = scanner.nextLine();
        password = getSHA512Hash(password);

        //Put username and password in auth
        String insertString = "INSERT INTO accounts VALUES(?,?)";
        PreparedStatement psmt = connection_auth.prepareStatement(insertString);
        psmt.setString(1,username);
        psmt.setString(2, password);
        psmt.executeUpdate();
        out.println("Account successfully created");
    }

    public static void createNewAccount(String token) throws SQLException {
        if(!validator(token))
        {
            out.println("Invalid token");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        out.println("Username: ");
        String username = scanner.nextLine();
        out.println("Password: ");
        String password = scanner.nextLine();
        password = getSHA512Hash(password);

        //Put username and password in auth
        String insertString = "INSERT INTO accounts VALUES(?,?)";
        PreparedStatement psmt = connection_auth.prepareStatement(insertString);
        psmt.setString(1,username);
        psmt.setString(2, password);
        psmt.executeUpdate();
        out.println("Account successfully created");
    }
    public static void db_read_menu() throws SQLException {

            String query = "SELECT * FROM menu";
            Statement s = connection_menu.createStatement();
            ResultSet set = s.executeQuery(query);
            out.println("Item No" + "|\t" + "Item" + "|\t" + "Price");
            while(set.next())
            {
                out.println(set.getString("sno") + " |\t "
                        + set.getString("item") + " |\t "
                        + set.getString("price"));
            }
    }

    public static void read_active_order(int cid) throws SQLException {
        String query = String.format("SELECT * FROM activeorders WHERE cid = %d;",cid);
        Statement s = connection_order.createStatement();
        ResultSet set = s.executeQuery(query);
        out.println("cid" +  "|\t" + "Bill");
        out.println(set.getString("cid") + " |\t " + set.getString("amount"));

        String query2 = "SELECT sno, item FROM menu WHERE sno in ("+set.getString("placedorder")+");";
        Statement s2 = connection_menu.createStatement();
        ResultSet set2 = s2.executeQuery(query2);
        out.println("sno"+"|\t"+"item");
        while(set2.next())
        {
            out.println(String.format("%d |\t %s",set2.getInt("sno"),set2.getString("item")));
        }
    }



    public static float bill_amt(String orderString) throws SQLException {
        String query = "SELECT sum(price) as bill from menu where sno in ("+orderString+");";
        Statement s = connection_menu.createStatement();
        ResultSet set = s.executeQuery(query);
        return set.getFloat("bill");
    }

    public static void place_order(int cid,String orderString,float amt) throws SQLException
    {
        String insertString = "INSERT INTO activeorders VALUES(?,?,?)";
        PreparedStatement psmt = connection_order.prepareStatement(insertString);
        psmt.setInt(1,cid);
        psmt.setString(2, orderString);
        psmt.setFloat(3,amt);
        psmt.executeUpdate();
    }

    public static void read_active_order() throws SQLException
    {
        String query = "SELECT * FROM activeorders;";
        Statement s = connection_order.createStatement();
        ResultSet set = s.executeQuery(query);
        out.println("cid"+"\t|"+"placedorder" +"\t|" +"amount");
        while(set.next())
        {
            out.println(String.format("%d \t| %s \t| %f",set.getInt("cid"),set.getString("placedorder"),set.getFloat("amount")));
        }
    }

    public static void insert_new_item(String token,String itemName, float itemPrice) throws SQLException {
        if(!DbHandler.validator(token))
        {
            System.out.println("Invalid token");
            return;
        }
        String insertString = "INSERT INTO menu(item,price) VALUES(?,?)";

        PreparedStatement psmt = connection_menu.prepareStatement(insertString);
        psmt.setString(1, itemName);
        psmt.setFloat(2,itemPrice);
        psmt.executeUpdate();
         out.println("Item added successfully");
    }

    public static void delete_from_menu(String token,String itemName) throws SQLException {
        if(!DbHandler.validator(token))
        {
            System.out.println("Invalid token");
            return;
        }
        String insertString = "delete from menu where item in (?)";

        PreparedStatement psmt = connection_menu.prepareStatement(insertString);
        psmt.setString(1, itemName);
        psmt.executeUpdate();
        out.println("Item deleted successfully");
    }

    public static void delete_from_menu(String token,int itemSNo) throws SQLException {
        if(!DbHandler.validator(token))
        {
            System.out.println("Invalid token");
            return;
        }
        String insertString = "delete from menu where sno in (?)";

        PreparedStatement psmt = connection_menu.prepareStatement(insertString);
        psmt.setInt(1, itemSNo);
        psmt.executeUpdate();
        out.println("Item deleted successfully");
    }

    public static void view_feedback() throws SQLException {
        String query = "SELECT * FROM feedback;";
        Statement s = connection_feedback.createStatement();
        ResultSet set = s.executeQuery(query);
        out.println("Email"+"\t|"+"Phone Number" +"\t|" +"No of People" +"\t|" + "Rating" +"\t|" + "Message");
        while(set.next())
        {
            out.println(String.format("%s \t| %d \t| %d \t| %f \t| %s",set.getString("email"),set.getInt("phoneno"),
                    set.getInt("groupsize"),set.getFloat("rating"),set.getString("message")));
        }
    }

    public static void insert_feedback(String email, String phoneno, int groupsize, float rating, String message) throws SQLException {
        String insertString = "INSERT INTO feedback VALUES(?,?,?,?,?)";

        PreparedStatement psmt = connection_feedback.prepareStatement(insertString);
        psmt.setString(1, email);
        psmt.setString(2,phoneno);
        psmt.setInt(3,groupsize);
        psmt.setFloat(4,rating);
        psmt.setString(5,message);
        psmt.executeUpdate();
        out.println("Thank You for your valuable feedback");
    }

    public static String getSHA512Hash(String plaintext)
    {
        try {
            
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            
            byte[] messageDigest = md.digest(plaintext.getBytes());

            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value 
            String hashcode = no.toString(16);

            // Add preceding 0s to make it 32 bit 
            while (hashcode.length() < 32) {
                hashcode = "0" + hashcode;
            }

            // return the HashCode
            return hashcode;
        }

        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String login(String username,String password) throws SQLException {
        password = getSHA512Hash(password);
        //dfb9ddec1d5776c6d633e9fc0b425715f9df21718caca990cd0b7722f73829e536fd42865fab1ddb965550e348cf9ea1bebbcd38f9c592e9e15217222fe1b8a9
        //Check if user exists
        String query = String.format("SELECT * FROM accounts where username='%s';",username);
        Statement s = connection_auth.createStatement();
        ResultSet userExistsTable = s.executeQuery(query);
        if(userExistsTable.next())
        {
           String actualPassword = userExistsTable.getString("password");
           if(actualPassword.equals(password))
           {
               out.println("Authentication Successful... \nPress Enter to continue");
               return actualPassword;
           }
        }
        else {
            out.println("Wrong username or password");
            Main.menu();
        }
        return password;
    }

}
