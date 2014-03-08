package uk.ac.lancs.LUFELFv2.commsV2;

/**
 * Created by Luke on 06/03/14.
 */
public class User {

    private int id;
    private String name;
    private String username;
    private String libaryCard;
    private String dob;
    private String type;
    private String description;

    public User(Integer id) {
        this. id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLibaryCard(String libaryCard) {
        this.libaryCard = libaryCard;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getId() {
        return Integer.toString(id);
    }

    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("id=" + id);
        b.append(",name=" + name);
        b.append(",libaryCard=" + libaryCard);
        b.append(",username=" + username);
        b.append(",dob=" + dob);
        b.append(",type=" + name);
        b.append(",description=" + description);

        return b.toString();
    }
}
