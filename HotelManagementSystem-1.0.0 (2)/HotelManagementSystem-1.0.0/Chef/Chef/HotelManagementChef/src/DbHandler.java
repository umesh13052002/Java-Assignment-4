import java.sql.*;
import java.util.Scanner;

import static java.lang.System.out;

public class DbHandler {
    static Connection connection_order = null;
    static String jdbcURL_order = "jdbc:sqlite:C:\\HotelData\\orders.sqlite";

    public static void connect()
    {
        try {
            connection_order = DriverManager.getConnection(jdbcURL_order);
        } catch (SQLException e) {
            out.println("Error connecting");
            e.printStackTrace();
        }
    }
    public static void read_active_order() throws SQLException
    {
        String query = "SELECT * FROM activeorders;";
        Statement s = connection_order.createStatement();
        ResultSet set = s.executeQuery(query);

        out.println("Press 'e' to return back");
        out.println("cid"+"\t|"+"placedorder" +"\t|" +"amount");

        while(set.next())
        {
            out.printf("%d \t| %s \t| %f%n",set.getInt("cid"),set.getString("placedorder"),set.getFloat("amount"));
        }
            ChefConsole.console_menu();
    }
    public static void remove_completed(int cid) throws SQLException {
        out.println("Processing cid = " +cid);
        String insertString = "delete from activeorders where cid in (?)";
        PreparedStatement psmt = connection_order.prepareStatement(insertString);
        psmt.setInt(1,cid);
        psmt.executeUpdate();
    }
}
