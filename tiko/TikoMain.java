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
import java.sql.Savepoint;
// time
import java.time.LocalDate;
import java.time.ZoneId;
// Regex
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tiko.User;

// @todo add SQL connection
class TikoMain
{
    /// Helpers for printin and formating
    public static void log(String tag, String str)
    {
        System.out.println(tag + ": " + str);
    }

    public static void print(String str)
    {
        System.out.print(str + " ");
    }

    public static void println(String str)
    {
        System.out.println(str);
    }

    public static void error(String str)
    {
        System.err.println(str);
    }

    /// Convert an array of Strings into an SQL block
    /// format: 'arr[0]', arr[1], ... , arr[n]
    public static String toSQL(String [] arr)
    {
        String res = "";
        for (String s : arr)
        {
            // comma-separate except for last element
            if(res.length() != 0)
            { res += (","); }
            // add sql string quotes
            res += ("'" + s +  "'");
        }

        return res;
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

    public static boolean loggedIn(User user)
    {
        boolean ret = (null != user && user.valid());
        if(!ret)
        {
            println("You are not logged in.");
        }

        return ret;
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
                login(conn, user);
                return true;
            case "register":
                register(conn);
                return true;
            case "info":
                info(user);
                return true;
            case "add_to_cart":
                // @todo fix the book id
                addToCart(conn, user, -1);
                return true;
            case "order":
                order(conn, user);
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
    static final String email_regex = "\\S+@\\S+\\.\\w+";
    static final String password_regex = "\\S{6}\\S*";
    // @todo Address needs to have whitespace
    static final String address_regex = ".+";
    // @todo how many numbers? 6-10
    // @todo this needs the option for +358 numbers (or other country codes)
    static final String phone_regex = "\\d{6,10}";

    /// Connect to the SQL server and verify
    /// Keeps asking till user provides correct username/password
    /// @todo provide a method for escaping from the loop
    /// @todo return a User when connected
    /// @return true if logged in succesfully, false otherwise
    public static boolean login(Connection conn, User user)
    {
        log("TRACE", "login");

        String username = inputPrompt("username", email_regex, "");
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
                String priv = rs.getString("kayttooikeus");
                if (priv.trim().equals("admin"))
                { user.admin = true; }

                // check the database for the user and password
                // if found return user struct
                // if not repromt (invalid username)
                println("Logged in Success");

                rs.close();
                stmt.close();

                return true;
            }
            else {
                rs.close();
                stmt.close();

                print("Invalid username or password");
                return login(conn, user);
            }

        } catch (SQLException e) {
             e.printStackTrace();
             error(e.getClass().getName()+": "+e.getMessage());
        }

        return false;
    }

    /// Print the information of the user
    public static void info(User user)
    {
        if(!loggedIn(user))
        { return; }

        println(user.toString());
    }

    /// register command has a form: register username password
    /// @return true if registered, false otherwise
    /// doesn't return User object by design, you have to login afterwards
    public static boolean register(Connection conn)
    {
        log("TRACE", "register");

        String pw_help = "at least 6 characters long, no whitespace";
        String email_help = "at least 6 characters long, no whitespace";

        // @todo check that username (email) is unique from the db
        // checking if the username is available before asking all the other details
        // would be good UI design
        String email = inputPrompt("username", email_regex, email_help);
        String password = inputPrompt("password", password_regex, pw_help);
        // @todo name regex
        String name = inputPrompt("Name", address_regex, "");
        // @todo add confirm password
        // @todo fix regex
        String address = inputPrompt("Address", address_regex, "");
        // @todo this needs space removal (of the input number)
        String phone = inputPrompt("phonenumber", phone_regex, "");

        Savepoint savePoint = null;
        try {
            savePoint = conn.setSavepoint("reg");
            Statement stmt = conn.createStatement();
            stmt.execute("SET search_path to keskus");
            String []arr = {email, name, password, address, phone, "user"};
            String sql = "INSERT INTO kayttaja " // "(email, name, password, address, phone, priviledge) "
                + "VALUES (" + toSQL(arr) + ")";
            log("DEBUG", sql);
            stmt.executeUpdate(sql);

            stmt.close();
            conn.commit();

            return true;
        } catch (SQLException e) {
            // @todo check that this is right error type
            // Aight vendor specific error codes and what not... fix this at some point.
            println("USER already existed");
            error("Error: " + e.getMessage());
            // fuck java exceptions
            try {
                if(savePoint != null)
                { conn.rollback(savePoint); }
            } catch(Exception _e) {}
        }
        return false;
    }

    /// @return true if added succesfully, false otherwise
    /// Iffy design decission to have the cart only for user that are logged in
    /// in a real application we would use a temporary cart/user for this
    ///
    /// We assume that the bookId is valid and existing
    /// so we can just push it directly into the SQL
    /// @todo
    /// need to add orderId into User if it's negative there is no order yet
    /// if there is no order yet create a new one (SQL command)
    /// if it's positive -> it exists -> update that order in the SQL
    public static boolean addToCart(Connection conn, User user, int bookId)
    {
        if(!loggedIn(user))
        { return false; }

        // @todo check that the book id is valid
        // i.e. it's not < and it's in the Database
        // need one more query here for that

        // @todo what is the logic with Statements
        // do we close them only after finishing (finally block)
        // while reusing them here?
        // or do we need to close them before reuse
        try {
            // @todo need to add, if not existing
            // so we need to add an extra field to the user or smth?
            // yea, empty field that we start filling the first time user calls add to cart
            // tilaus (tilaus_id, kayttajaref, pvm, tila)

            // Find an already existing order where to add
            Statement stmt = conn.createStatement();
            stmt.execute("SET search_path to keskus");
            String query = "select * from tilaus where " +
                "tilaaja='" + user.email +"'" + " and " +
                "tila='avoin'" +
                ";";
            stmt.executeQuery(query);
            int orderId = -1;
            ResultSet rs = stmt.executeQuery(query);
            // found an order
            if ( rs.next() ) {
                orderId = rs.getInt("nro");

                rs.close();
                stmt.close();
            }
            // else take a count of the array and use that as new id
            // create a new order with the id
            else {
                stmt.execute("SET search_path to keskus");
                query = "select count(*) from tilaus";
                stmt.executeQuery(query);
                rs = stmt.executeQuery(query);
                if ( rs.next() ) {
                    orderId = rs.getInt("count");
                }
                else {
                    error("Something really fucked up with count.");
                }

                println("Cool now we need to add a new order: " + orderId);

                PreparedStatement pstm = conn.prepareStatement(
                        "insert into tilaus"
                      + " (nro, tilaaja, pvm, tila)"
                      + " values (?, ?, ?, ?)");
                pstm.setInt(1, orderId);
                pstm.setString(2, user.email);
                pstm.setObject( 3, LocalDate.now(ZoneId.of( "Europe/Helsinki" ) ));
                pstm.setString(4, "avoin");

                pstm.executeUpdate();

                pstm.close();
                conn.commit();
            }

            if(orderId < 0)
            {
                error("Something really fucked up with order ID.");
                return false;
            }

            println("Cool now we need to update order: " + orderId);

            // do the update
            // @todo test, seems to work but we need the SQL table kirjat and valid id
            PreparedStatement pstm = conn.prepareStatement(
                    "insert into tilaus_kirjat"
                  + " (tilaus_nro, kirja_nro)"
                  + " values (?, ?)");
            pstm.setInt(1, orderId);
            pstm.setInt(2, bookId);

            pstm.executeUpdate();

            pstm.close();
            conn.commit();

        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }

        return false;
    }

    /// @return true if ordered succesfully, false otherwise
    public static boolean order(Connection conn, User user)
    {
        if(!loggedIn(user))
        { return false; }

        // @todo check the user.order field if it has an order
        // yes -> retrieve the order from SQL
        //        send it to the user
        //        update it's state in the SQL
        //        clear the user.order to -1
        // no ->  exit with an error

        return false;
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

