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
    }
}

