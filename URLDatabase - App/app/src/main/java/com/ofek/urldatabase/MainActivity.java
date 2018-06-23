package com.ofek.urldatabase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    public static Client c;
    public final static String IP = "";
    public final static int PORT = 0;
    private Intent intent;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Login"); // setting the action bar title

        // creating only one client
        if (c == null)
        {
            c = new Client(IP, PORT, this);
            c.start();
        }

        EditText passwordT = (EditText) findViewById(R.id.password);
        passwordT.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // setting an action when the user pressed enter
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    Button login = (Button) findViewById(R.id.btn_login);
                    login.callOnClick();
                    return true;
                }

                return false;
            }
        });

        intent = new Intent(this, MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.my_html_menu_item:
                Intent i = new Intent(this, HTMLDisplayActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loginAction(View v)
    {
        // getting the username and password
        String username = ((EditText) findViewById(R.id.username)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();

        if (username.length() > 16 || username.length() < 0 || password.length() > 16 || password.length() < 0)
            Toast.makeText(this, "Username and password mast be between 1 - 16.", Toast.LENGTH_SHORT).show();
        else
        {
            // hashing the password for security purposes
            SHA3.DigestSHA3 hash = new SHA3.Digest512();
            byte[] digest = hash.digest(password.getBytes());
            password = Hex.toHexString(digest);

            HashMap<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username);
            userInfo.put("password", password);

            // telling the client to calling the login command
            c.setCommand(1, userInfo);
        }
    }

    // opening the register activity
    public void registerOpenActivity(View v)
    {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy()
    {
        long time = 1000 * 60 * 60;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, time, pendingIntent);

        // telling the client thread to close
        c.setCommand(6);

        super.onDestroy();
    }
}
