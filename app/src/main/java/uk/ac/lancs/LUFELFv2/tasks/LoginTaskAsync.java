package uk.ac.lancs.LUFELFv2.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import uk.ac.lancs.LUFELFv2.commsV2.APIException;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;

/**
 * Created by Luke on 06/03/14.
 */
public class LoginTaskAsync extends AsyncTask<String, Void, String> {
    private Handler handler;
    private Context context;
    private String username;
    private String password;
    private ProgressDialog dialog;
    private boolean success;
    private ServerFactory factory = ServerFactory.getInstance();

    public LoginTaskAsync(Handler handler, Context context, String username, String password){
        this.handler = handler;
        this.context = context;
        this.username = username;
        this.password = password;
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
        try {
            factory.login(username, password);
        } catch (APIException e) {
        }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Message msg = new Message();
        msg.obj = factory.isLoggedIn();
        handler.sendMessage(msg);

        if(factory.isLoggedIn()) {
            Toast.makeText(context, "Logged in as " + factory.getUser().getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unable to login", Toast.LENGTH_SHORT).show();
        }
    }
}
