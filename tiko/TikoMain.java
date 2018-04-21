/** Joonatan Kuosa
 *  Aki Lempola
 *
 *  TIKO 2018 harjtyo
 */


package tiko;

// User input
import java.util.Scanner;
// SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

// @todo add SQL connection
class TikoMain
{
    /// Helpers for printin and formating
    public static void print(String str)
    {
        System.out.println(str);
    }

    public static void error(String str)
    {
        System.err.println(str);
    }

    /// Return false if program should exit
    /// True otherwise
    /// Executes the users commands
    public static boolean parseCmd(String command)
    {
        // @todo this should be ArrayList with each parameter separated
        String params = "";
        // @todo parse cmd into arguments (split from spaces)
        // command into it's own string and params into an array list
        String cmd = command;
        switch(cmd)
        {
            case "login":
                login(params);
                return true;
            case "register":
                register(params);
                return true;
            case "exit":
            case "quit":
                return false;
            default:
                error("Faulty command.");
        }
        return true;
    }

    public static void login(String params)
    {
        // @todo add SQL commands
        print("login: " + params);
    }

    public static void register(String params)
    {
        // @todo add SQL commands
        print("register: " + params);
    }

    // insert book info to the div1 database
    public static void addBook(Connection c)
    {
        // @todo check user-role and permission
        // @todo change database to the corresponding schema
        try {
            /* test hack */
            Statement stmt = c.createStatement();
            stmt.execute("SET search_path to div1");
            print("changed to div1");
            stmt.close();
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
        
        String sql = "INSERT INTO kirja VALUES(?, ?, ?, ?, ?, ?)";
        // @todo query user input
        // @todo find out how to generate ids automaticly (in sql?)
        int id = 1;
        String author = "author";
        String name = "name";
        String type = "type";
        String category = "category";
        String isbn = "123-123";
        try {
            PreparedStatement addBook = c.prepareStatement(sql);
            addBook.setInt(1, id);
            addBook.setString(2, author);
            addBook.setString(3, name);
            addBook.setString(4, type);
            addBook.setString(5, category);
            addBook.setString(6, isbn);
            int ret = addBook.executeUpdate();
            print("Added " + ret + " book to kirja");
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }


    public static void addCopy(Connection c)
    {
        // @todo check user-role and permission
        // @todo change database to the corresponding schema
        
        // @todo query existing book, and divari
        String insertTeos = "INSET INTO teos VALUES (?, ?, ?, ?)";
        // @todo read input
        float paino = 0.0f;
        int kirjaNro = 0;
        float hinta = 0.0f;
        int divariId = 0;
        try {
            PreparedStatement addCopy = c.prepareStatement(insertTeos);
            addCopy.setFloat(1, paino);
            addCopy.setInt(2, kirjaNro);
            addCopy.setFloat(3, hinta);  // @todo sql: add hinta to sql teos table
            addCopy.setInt(4, divariId); // @todo sql: add divari_nro to teos table
            int ret = addCopy.executeUpdate();
            addCopy.close();
            print("Added " + ret + " book to teos");
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }

    /// Read user commands and sent them to the parser
    public static void main(String [] args)
    {
        Connection c = null;
        try {
            String username = "postgres";
            String password = "postgres";
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/divari",
                username, password);
        } catch (Exception e) {
             e.printStackTrace();
             error(e.getClass().getName()+": "+e.getMessage());
             System.exit(0);
        }
        print("Opened database successfully");

        Scanner scanner = new Scanner(System.in);
        String line = "";
        while (true)
        {
            line = scanner.nextLine();
            if (!parseCmd(line))
            { break; }
        }
        scanner.close();
    }
}

