/** Joonatan Kuosa
 *  Aki Lempola
 *
 *  TIKO 2018 harjtyo
 */

package tiko;

public class BookInfo
{
    private String author;
    private String name;
    private String type;
    private String category;
    private String isbn;
    private int id;
    private float weight;
    private float price;

    private String shopName;
    private String shopAddress;

    public BookInfo(int id, String author, String name, String type, String category,
        String isbn,  float weight, float price, String shopName,
        String shopAddress) {
            this.author = author;
            this.name = name;
            this.category = category;
            this.type = type;
            this.isbn = isbn;
            this.id = id;
            this.weight = weight;
            this.price = price;
            this.shopName = shopName;
            this.shopAddress = shopAddress;
    }

    public String toString() {
        return (" " + id + ": " +name+"; " +author+": " +type+ "; " +category+ ": isbn: " +isbn+ "\n"
            + "\tprice: " +price+ "e; Shop: " + shopName );
    }
};

