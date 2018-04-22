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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
// Regex
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tiko.User;

// @todo add SQL connection
class TikoMain
{
    public static void log(String tag, String str)
    {
        System.out.println(tag + ": " + str);
    }

    /// Helpers for printin and formating
    public static void print(String str)
    {
        System.out.println(str);
    }

    public static void error(String str)
    {
        System.err.println(str);
    }

    public static String inputPrompt(String fieldName, String regex, String help)
    {
        Scanner scanner = new Scanner(System.in);

        // @todo print help
        while (true)
        {
            print(fieldName + ":" );
            String line = scanner.nextLine();
            // if line matches regex
            // return line
            Pattern p = Pattern.compile(regex);
            if(p.matcher(line).matches())
            { return line; }
        }
    }

    /// Return false if program should exit
    /// True otherwise
    /// Executes the users commands
    public static boolean parseCmd(Connection conn, User user, String command)
    {
        // @todo this should be ArrayList with each parameter separated
        String[] arr = command.split(" ", 0);
        String cmd = arr[0];
        String[] params = new String[arr.length-1];
        System.arraycopy(arr, 1, params, 0, arr.length-1);
        // @todo parse cmd into arguments (split from spaces)
        // command into it's own string and params into an array list
        switch(cmd)
        {
            case "login":
                login(conn, user, params);
                return true;
            case "register":
                register(conn, params);
                return true;
            case "exit":
            case "quit":
                return false;
            default:
                error("Faulty command.");
        }
        return true;
    }

    // todo username==email, fix regex to that format
    static final String username_regex = "\\w+";
    static final String password_regex = "\\S{6}\\S*";
    // @todo Address needs to have whitespace
    static final String address_regex = "\\w+";
    // @todo how many numbers?
    static final String phone_regex = "\\d+";

    /// Connect to the SQL server and verify
    /// Keeps asking till user provides correct username/password
    /// @todo provide a method for escaping from the loop
    /// @todo return a User when connected
    public static void login(Connection conn, User user, String[] params)
    {
        log("TRACE", "login: " + "with " + params.length + " params");

        String username = inputPrompt("username", password_regex, "");
        String pw = inputPrompt("password", password_regex, "");

        log("TRACE", "username = " + username);
        log("TRACE", "password = " + pw);
        // select statement
        try {
            Statement stmt = conn.createStatement(
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY );
            boolean ret = stmt.execute( "SET SEARCH_PATH TO KESKUS;");
            String query = "select * from kayttaja where " +
                "email='" + username +"'" + " and " +
                "salasana='" + pw + "'" +
                ";";
            ResultSet rs = stmt.executeQuery(query);
            if ( rs.next() ) {
                user.email = rs.getString("email");
                user.name = rs.getString("nimi");
                // don't save the password
                user.address = rs.getString("osoite");
                user.phonenumber = rs.getString("puh_nro");

                // check the database for the user and password
                // if found return user struct
                // if not repromt (invalid username)
                print("LOGIN in");
                print(user.toString());

                rs.close();
                stmt.close();
            }
            else {
                rs.close();
                stmt.close();

                print("Invalid username or password");
                login(conn, user, params);
            }

        } catch (SQLException e) {
             e.printStackTrace();
             error(e.getClass().getName()+": "+e.getMessage());
        }
    }

    /// register command has a form: register username password
    /// @return true if registered, false otherwise
    /// doesn't return User object by design, you have to login afterwards
    public static void register(Connection conn, String[] params)
    {
        // @todo add SQL commands
        print("register: " + "with " + params.length + " params");

        String pw_help = "at least 6 characters long, no whitespace";

        String username = inputPrompt("username", username_regex, "");
        // @todo check that username (email) is unique from the db
        String password = inputPrompt("password", password_regex, pw_help);
        // @todo add confirm password
        // @todo fix regex
        inputPrompt("Address", address_regex, "");
        // @todo fix regex
        inputPrompt("phonenumber", phone_regex, "");
        // @todo fix regex
        //inputPrompt("email", "\\w+/g", "");
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
    /// Arguments
    ///     databasename
    ///     username (for db)
    ///     password (for db)
    /// for example: java tiko/TikoMain divari postgres postgres
    ///         or : run.bat divari postgres postgres
    ///         or : run.sh divari postgres postgres
    public static void main(String [] args)
    {
        Connection conn = null;
        try {
            String dbname = args.length > 0 ? args[0] : "divari";
            String username = args.length > 1 ? args[1] : "postgres";
            String password = args.length > 2 ? args[2] : "postgres";
            Class.forName("org.postgresql.Driver");
            conn = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/" + dbname,
                username, password);
            conn.setAutoCommit(false);

            log("TRACE", "Opened database successfully");

            Scanner scanner = new Scanner(System.in);
            String line = "";
            User user = new User();
            while (true)
            {
                // @todo print the logged in username (ala Linux) if the user has logged in
                if(user.valid())
                { System.out.print(user.email + " : "); }
                else
                { System.out.print("Command: "); }
                line = scanner.nextLine();
                if (!parseCmd(conn, user, line))
                { break; }
            }
            conn.close();
        } catch (Exception e) {
             e.printStackTrace();
             error(e.getClass().getName()+": "+e.getMessage());
             System.exit(0);
        }
    }
}

