import java.sql.SQLException;
import java.util.Scanner;

import static java.lang.System.out;
public class Manager {
    private final String token;

    public Manager(String token)
    {
        if(!DbHandler.validator(token))
        {
            System.out.println("Invalid token received");
        }
        this.token = token;
    }

    public void viewMenu() throws SQLException {
        DbHandler.db_read_menu();
        this.showManagementConsole();
    }

    public void addItemToMenu(String itemName,float itemPrice) throws SQLException {
       DbHandler.insert_new_item(token,itemName,itemPrice);
       this.showManagementConsole();
    }

    public void deleteItemFromMenu(String itemName) throws SQLException {
        DbHandler.delete_from_menu(token,itemName);
        this.showManagementConsole();
    }

    public void deleteItemFromMenu(int itemSNo) throws SQLException {
        DbHandler.delete_from_menu(token,itemSNo);
        this.showManagementConsole();
    }

    public void viewFeedback() throws SQLException {
        DbHandler.view_feedback();
        this.showManagementConsole();
    }

    public void  showManagementConsole() throws SQLException {

        Scanner scanner = new Scanner(System.in);
        out.println("Press Enter...");
        scanner.nextLine();
        String choice;
        out.println("----------------------Management Console----------------------");
        out.println("Menu:\n1. View Menu\n2. Add Item to menu\n3. Delete Item from menu\n4. View customer feedback\n5. Main Menu");
        choice = scanner.nextLine();

        switch(choice)
        {
            case "1":
                this.viewMenu();
                break;
            case "2":
                out.println("Enter item name: ");
                String itemName = scanner.nextLine();
                out.println("Enter item price: ");
                float price = Float.parseFloat(scanner.nextLine());
                this.addItemToMenu(itemName,price);
                break;
            case "3":
                out.println("Enter item no (sno) to be deleted: ");
                int itemno = Integer.parseInt(scanner.nextLine());
                this.deleteItemFromMenu(itemno);
                break;
            case "4":
                this.viewFeedback();
            case "5":
                Main.menu();
                break;
            default:
                out.println("Enter a valid option (1 to 4)");
                this.showManagementConsole();

        }



    }





}
