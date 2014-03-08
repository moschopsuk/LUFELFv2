package uk.ac.lancs.LUFELFv2.commsV2;

/**
 * Created by Luke on 06/03/14.
 */
public class APIException extends Exception {

    private int code;

    //Parameterless Constructor
    public APIException() {}

    //Constructor that accepts a message
    public APIException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
