/** Joonatan Kuosa
 *  Aki Lempola
 *
 *  TIKO 2018 harjtyo
 */

package tiko;

// c-style struct
class User
{
    User() {}

    public boolean valid()
    { return this.email != null; }

    // username==email
    public String email;
    // no field for a passwd, we don't store it
    public String name;
    public String address;
    public String phonenumber;
    public String priviledges;

    public String toString()
    {
        return (
            "EMAIL= " + this.email + '\n' +
            "NAME = " + this.name + '\n' +
            "ADDRESS = " + this.address + '\n' +
            "phonenumber = " + this.phonenumber + '\n'
            );
    }
};

