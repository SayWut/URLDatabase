package com.ofek.urldatabase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");

        EditText passwordT = (EditText) findViewById(R.id.reg_password);
        passwordT.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // setting an action when the user pressed enter
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    Button register = (Button) findViewById(R.id.btn_register);
                    register.callOnClick();
                    return true;
                }

                return false;
            }
        });
    }

    public void registerAction(View v)
    {
        // getting the username and password
        String username = ((EditText) findViewById(R.id.reg_username)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.reg_password)).getText().toString().trim();

        if (username.length() > 16 || username.length() < 0 || password.length() > 16 || password.length() < 0)
            Toast.makeText(this, "Please fill username and password.", Toast.LENGTH_SHORT).show();
        else
        {
            // hashing the password for security purposes
            SHA3.DigestSHA3 hash = new SHA3.Digest512();
            byte[] digset = hash.digest(password.getBytes());
            password = Hex.toHexString(digset);

            HashMap<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username);
            userInfo.put("password", password);
            userInfo.put("register", this);

            // telling the client to calling the register command
            MainActivity.c.setCommand(0, userInfo);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
