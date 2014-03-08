package uk.ac.lancs.LUFELFv2.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import uk.ac.lancs.LUFELFv2.commsV2.APIException;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;

/**
 * Created by Luke on 06/03/14.
 */
public class RegisterTaskAsync extends AsyncTask<String, Void, String> {
    private Handler handler;
    private Context context;
    private String username;
    private String password;
    private String libaryCard;
    private String name;
    private String dob;
    private Integer access;
    private Boolean hasCreated;
    private ProgressDialog dialog;
    private ServerFactory factory = ServerFactory.getInstance();

    public RegisterTaskAsync(Handler handler, Context context, String username, String password, String libaryCard, String name, String dob, Integer access){
        this.handler = handler;
        this.context = context;
        this.username = username;
        this.password = password;
        this.libaryCard = libaryCard;
        this.name = name;
        this.dob = dob;
        this.access = access;
        this.hasCreated = false;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        this.hasCreated = factory.register(username, password, libaryCard, name, dob, "blank", "blank", "0", access.toString());
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Message msg = new Message();
        msg.obj = this.hasCreated;
        handler.sendMessage(msg);

        if(this.hasCreated) {
            Toast.makeText(context, "Accounted Created", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unable to Create Account", Toast.LENGTH_SHORT).show();
        }
    }
}
