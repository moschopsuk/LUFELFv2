package uk.ac.lancs.LUFELFv2.commsV2;

/**
 * Created by Luke on 05/03/14.
 */
public class AppMessage {
    private String id;
    private String recipient;
    private String message;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
