/** Joonatan Kuosa
 *  Aki Lempola
 *
 *  TIKO 2018 harjtyo
 */


package tiko;

// User input
import java.util.Scanner;
import java.awt.print.Book;
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
// Data structure
import java.util.ArrayList;
import java.util.Arrays; // testing: printing arrays

import tiko.User;
import tiko.BookInfo;

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

    // prompts float allows user to use decimal ',' or '.'
    public static float promptFloat(String fieldName, String help)
    {
        String s = inputPrompt(fieldName, "\\d+(,|\\.)?\\d*", help);
        s = s.replace(',', '.');
        return Float.parseFloat( s );
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
                addToCart(conn, user, params);
                return true;
            case "cart":
                showCart(conn, user);
                return true;
            case "order":
                order(conn, user);
                return true;
            case "show_orders":
                showOrders(conn, user);
                return true;
            case "add_book":
                addBook(conn);
                return true;
            case "sell_book":
                sellBook(conn, null);
                return true;
            case "search":
                searchBooks(conn, params);
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


    // return id from table row where col = value
    // else return -1
    public static int getIdFromTable(Connection c,String table, String col, String value)
    {
        try {
            String sql = "SELECT nro FROM "+table
                + " WHERE "+col+" = ?";
            PreparedStatement getId = c.prepareStatement(sql);
            getId.setString(1, value);
            ResultSet rs = getId.executeQuery();
            if ( rs.next() ) {
                int id = rs.getInt("nro");
                getId.close();
                return id;
            }
        } catch (Exception e) {
            error("Error: " + e.getMessage());
        }
        return -1;
    }

    /** Print the users cart if one exists
     */
    public static void showCart(Connection conn, User user)
    {
        if (!loggedIn(user))
        { return; }

        Statement stmt;
        ResultSet rs;
        try {
            stmt = conn.createStatement();
            stmt.execute("SET search_path to keskus");
            // select kirja_nro from
            // (select * from tilaus where tilaaja='j@foo.bar') as t
            // inner join tilaus_kirjat on t.nro = tilaus_kirjat.tilaus_nro;
            String query =
                "select kirja_nro from "
              + "(select * from tilaus where tilaaja='" + user.email + "' and tila='avoin') as t "
              + " inner join tilaus_kirjat on t.nro = tilaus_kirjat.tilaus_nro; "
              + ";";
            stmt.executeQuery(query);
            rs = stmt.executeQuery(query);
            if(!rs.isBeforeFirst()) {
                println("No open orders.");
            }
            while( rs.next() )
            {
                // @todo print details about the book
                println("Book: " + rs.getInt("kirja_nro") + " in order.");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }

    // get next free id from table, -1 if some erros occuer
    // set to right search path prior calling this method
    //
    // ids start from 0, and increase by increments of 1
    public static int getNextFreeId(Connection c, String table)
    {
        int id = -1;
        try {
            String sql = "SELECT COUNT( nro ) FROM " + table;
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
                id = rs.getInt("count");
                stmt.close();
                return id;
        } catch (Exception e) {
            error("Error: " + e.getMessage());
        }
        return id;
    }

    /** Iffy design decission to have the cart only for user that are logged in
     *  in a real application we would use a temporary cart/user for this
     *
     *  Orders are saved in the Database, so retrieves and creates one there
     *
     * @param conn : database connection
     * @param user : logged in user
     * @param params : commandline params (bookId as params[0])
     * @return true if added succesfully, false otherwise
     */
    public static boolean addToCart(Connection conn, User user, String [] params)
    {
        if(!loggedIn(user))
        { return false; }

        if(params.length <= 0)
        {
            error("Please give a book id as parameter");
            return false;
        }

        int bookId = Integer.parseInt(params[0]);

        log("TRACE", "addToCart with " + bookId + " book.");

        Statement stmt;
        PreparedStatement pstm;
        ResultSet rs;
        try {
            // Check that the book is in the DB
            stmt = conn.createStatement();
            stmt.execute("SET search_path to keskus");
            String query = "select nro from kirja where " +
                " nro=" + bookId +
                ";";
            stmt.executeQuery(query);
            rs = stmt.executeQuery(query);
            if ( !rs.next() )
            {
                error("Book with id " + bookId + " not found.");
                return false;
            }
            rs.close();
            stmt.close();

            // Find an already existing order where to add
            stmt = conn.createStatement();
            query = "select * from tilaus where " +
                "tilaaja='" + user.email +"'" + " and " +
                "tila='avoin'" +
                ";";
            stmt.executeQuery(query);
            int orderId = -1;
            rs = stmt.executeQuery(query);
            // found an order
            if ( rs.next() ) {
                orderId = rs.getInt("nro");

                rs.close();
                stmt.close();
            }
            // else take a count of the array and use that as a new order id
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

                log("TRACE", "Cool now we need to add a new order: " + orderId);

                pstm = conn.prepareStatement(
                        "insert into tilaus"
                      + " (nro, tilaaja, pvm, tila)"
                      + " values (?, ?, ?, ?)");
                pstm.setInt(1, orderId);
                pstm.setString(2, user.email);
                pstm.setObject( 3, LocalDate.now(ZoneId.of( "Europe/Helsinki" ) ));
                pstm.setString(4, "avoin");

                pstm.executeUpdate();

                conn.commit();
                pstm.close();
            }

            if(orderId < 0)
            {
                error("Something really fucked up with order ID.");
                return false;
            }

            log("TRACE", "Cool now we need to update order: " + orderId);

            // Add to cart
            pstm = conn.prepareStatement(
                    "insert into tilaus_kirjat"
                  + " (tilaus_nro, kirja_nro)"
                  + " values (?, ?)");
            pstm.setInt(1, orderId);
            pstm.setInt(2, bookId);

            pstm.executeUpdate();

            conn.commit();
            pstm.close();

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
        // Find an already existing order where to add
        Statement stmt;
        String query;
        try {
            stmt = conn.createStatement();
            query = "select * from tilaus where " +
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

                // Do order
                PreparedStatement pstm = conn.prepareStatement(
                        "update tilaus set tila =? where nro =?");
                pstm.setString(1, "maksettu");
                pstm.setInt(2, orderId);
                pstm.executeUpdate();

                pstm.close();
                conn.commit();

            }
            stmt.close();
        } catch (Exception e) {
            error("Error: " + e.getMessage());
        }
        return false;
    }

    /** Print all the orders by this user
     *
     *  @param conn Connection to Database
     *  @param user whose orders we print
     */
    public static void showOrders(Connection conn, User user)
    {
        try {
            Statement stmt = conn.createStatement();
            String query =
                "select * from tilaus where "
              + "tilaaja='" + user.email +"'"
              + ";";

            stmt.execute("SET SEARCH_PATH TO keskus");
            ResultSet rs = stmt.executeQuery(query);

            while ( rs.next() ) {
                int orderId = rs.getInt("nro");
                println("Order: " + orderId);
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            error("Error: " + e.getMessage());
        }

    }

    // prints books that fulfil search criteria
    public static void searchBooks(Connection c, String[] params)
    {
        String instruction = "Crtiteria: use #title #author #category or"
            + " #type to specify where to search\n";
        String input = inputPrompt(instruction, ".*", "");
        ArrayList<BookInfo> books = listAvaibleBooks(c);
        if (input.length() == 0) {
            for (BookInfo book : books) {
                println(book.toString());
            }
        }
        else {
            String[] inputSplit = input.split("#");
            for (String str : inputSplit) {
                String[] split = str.split(" ", 2);

                if (split.length < 2) {
                    continue;
                }
                split[1] = split[1].trim();
                ArrayList<BookInfo> in = new ArrayList<>(books);
                switch (split[0]) {
                    case "title":
                        for (BookInfo book : in) {
                            if ( !book.name().toLowerCase().contains(split[1].toLowerCase())) {
                                books.remove(book);
                            }
                        }
                        break;
                    case "author":
                        for (BookInfo book : in) {
                            if ( !book.author().toLowerCase().contains(split[1].toLowerCase())) {
                                books.remove(book);
                            }
                        }
                        break;
                    case "category":
                        for (BookInfo book : in) {
                            if ( !book.category().toLowerCase().contains(split[1].toLowerCase())) {
                                books.remove(book);
                            }
                        }
                        break;
                    case "type":
                        for (BookInfo book : in) {
                            if ( !book.type().toLowerCase().contains(split[1].toLowerCase()) ) {
                                books.remove(book);
                            }
                        }
                        break;
                    default:
                        break;
                } // end switch
            } // end for each

            if (books.size() == 0) {
                println("0 books found.");
            } else {
                for (BookInfo book : books) {
                    println(book.toString());
                }
            }
        } // end else
    }

    // query all avaible books from keskus.
    // return books in ArrayList
    public static ArrayList<BookInfo> listAvaibleBooks(Connection c)
    {
        ArrayList<BookInfo> books = new ArrayList<BookInfo>();
        try {
            // @todo ääkköset katoaa?
            Statement stmt = c.createStatement();
            stmt.execute("SET SEARCH_PATH TO keskus");
            ResultSet rs = stmt.executeQuery("SELECT * FROM myynnissa");
            while(rs.next()) {
                int id = rs.getInt("nro");
                String name = rs.getString("nimi");
                String author = rs.getString("tekija");
                String type = rs.getString("tyyppi");
                String category = rs.getString("luokka");
                String isbn = rs.getString("isbn");
                float weight = rs.getFloat("paino");
                float price = rs.getFloat("hinta");
                String shopName = rs.getString("divari_nimi");
                String shopAddress = rs.getString("osoite");

                BookInfo b = new BookInfo(id, author, name, type, category, isbn,
                    weight, price, shopName, shopAddress);
                books.add(b);
            }
        } catch (Exception e) {
            error("Error: " + e.getMessage());
        }
        return books;
    }

    // insert book info to the div1 and keskus
    //
    public static void addBook(Connection c)
    {
        // @todo check user-role and permission
        // @todo change database to the corresponding schema
        // @todo add params: database name
        // (nro int, tekija string, nimi string, tyyppi string, luokka string, isbn string)
        String sqlBook = "INSERT INTO kirja VALUES(?, ?, ?, ?, ?, ?)";

        try {
            Statement stmt = c.createStatement();
            stmt.execute("SET search_path to div1");
            println("changed to div1");

            String isbn = inputPrompt("isbn", ".*", "");
            int bookId = getIdFromTable(c, "kirja", "isbn", isbn);

            if (bookId >= 0) { // book found
                // @todo book found just add selling copy
                println("BookId found:" + bookId);
            }
            // add new book info to the database
            else {
                String author = inputPrompt("author", ".+", "Author of the book");
                String name = inputPrompt("book name", ".+", "Name of the book");
                String type = inputPrompt("type", ".+", "e.g. novel or comic book");
                String category = inputPrompt("Category", ".+", "e.g. romance or humor");


                bookId = getNextFreeId(c, "kirja");

                PreparedStatement addBook = c.prepareStatement(sqlBook);
                // @todo set empty strings to null or dont allow empty values
                addBook.setInt(1, bookId);
                addBook.setString(2, author);
                addBook.setString(3, name);
                addBook.setString(4, type);
                addBook.setString(5, category);
                addBook.setString(6, isbn);
                int ret = addBook.executeUpdate();
                println("  Added " + ret + " book to kirja");
                // trying to add book info to keskus
                try {
                    stmt.execute("SET search_path to keskus");
                    int bookKeskus = getIdFromTable(c, "kirja", "isbn", isbn);
                    if (bookKeskus < 0 ) {
                        addBook.setInt(1, getNextFreeId(c, "kirja") );
                        addBook.executeUpdate();
                    }
                    stmt.execute("SET search_path to div1");
                } catch (Exception e) {
                    stmt.execute("SET search_path to div1");
                }
                addBook.close();
            }
            c.commit();

            boolean addCopy = inputPrompt("add book to stock (y/n)", "(y|n)", "").equals("y");
            if (addCopy) {
                addCopyDiv1(c, bookId);
                if ( inputPrompt("sell in hub (y/n)", "(y|n)", "").equals("y") ) {
                    sellBook(c, isbn);
                }
            }
            stmt.close();
            c.commit();
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }

    // add selling copy to div1 database
    // @todo how and when div1 adds books to keskus
    public static void addCopyDiv1(Connection c, int bookId)
    {
        // (nro int, paino float, kirja_nro int, ostohinta float)
        String sql = "INSERT INTO teos(nro, paino, kirja_nro, ostohinta)"
            + " VALUES(?, ?, ?, ?)";
        int copyId = 1;
        // TODO try getting weight from teos
        float weight = promptFloat("weight(kg)", "weight in kilograms");
        float buyin = promptFloat("buyin price", "");

        try {
            Statement stmt = c.createStatement();
            stmt.execute("SET search_path to div1");
            copyId = getNextFreeId(c, "teos");

            PreparedStatement addCopy = c.prepareStatement(sql);
            addCopy.setInt(1, copyId);
            addCopy.setFloat(2, weight);
            addCopy.setInt(3, bookId);
            addCopy.setFloat(4, buyin);
            int ret = addCopy.executeUpdate();
            println("Added " + ret + " selling copy to teos");

            stmt.close();
            addCopy.close();
            c.commit();
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }

    // gets bookId from keskus.kirja and adds selling copy
    // to keskus.
    public static void sellBook(Connection c, String isbn)
    {
        // @todo check permission
        try {
            Statement stmt = c.createStatement();
            stmt.execute("SET SEARCH_PATH TO keskus");
            // @todo better way getting bookId, books can have same name
            if (isbn == null) {
                isbn = inputPrompt("book isbn", ".+", "");
            }

            int bookId = getIdFromTable(c, "kirja", "isbn", isbn);
            if ( bookId < 0 ) {
                println( isbn + " is not in database" );
                return;
            }

            boolean ret = addCopyHub(c, bookId);
            if (ret) {
                println("Added selling copy to keskus");
            }
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
        }
    }

    // adds selling copy to the keskus.
    // book info needs to be in keskus.kirja
    public static boolean addCopyHub(Connection c, int bookId)
    {
        // @todo check user-role and permission
        // @todo change database to the corresponding schema

        // @todo query existing book, and divari
        // teos(nro int, paino float, kirja_nro int, div_nro int,
        //     hinta float, tilaus_nro int)
        int copyId = -1;

        try {
            Statement stmt = c.createStatement();
            stmt.execute("SET SEARCH_PATH TO keskus");
            String insertTeos = "INSERT INTO teos VALUES (?, ?, ?, ?, ?, ?)";
            String shopName = inputPrompt("Shop name", ".+", "");
            // @todo try getting shop id from admin rights?
            int shopId = getIdFromTable(c, "divari", "nimi", shopName);
            if ( shopId < 0 ) {
                println("No such shop as: " + shopName);
                return false;
            }
            // @todo try getting weight from teos
            float weight = promptFloat("weight", "book weight in kilograms");
            float sellPrice = promptFloat("selling price", "");

            copyId = getNextFreeId(c, "teos");

            PreparedStatement addCopy = c.prepareStatement(insertTeos);
            addCopy.setInt(1, copyId);
            addCopy.setFloat(2, weight);
            addCopy.setInt(3, bookId);
            addCopy.setFloat(4, sellPrice);
            addCopy.setInt(5, shopId);
            addCopy.setNull(6, java.sql.Types.NULL );
            int ret = addCopy.executeUpdate();
            addCopy.close();
            c.commit();
            return true;
        } catch (SQLException e) {
            error("Error: " + e.getMessage());
            return false;
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

