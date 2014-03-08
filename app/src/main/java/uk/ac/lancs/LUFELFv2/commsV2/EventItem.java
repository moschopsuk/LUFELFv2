package uk.ac.lancs.LUFELFv2.commsV2;

/**
 * Created by Luke on 06/03/14.
 */
public class EventItem {
    private int id;
    private String name;
    private String created;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCreated() {
        return this.created;
    }
}
