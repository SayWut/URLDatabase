package com.ofek.urldatabase;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Client extends Thread
{
    private static Socket client;
    private static ObjectInputStream is;
    private static ObjectOutputStream os;

    private static String IP;
    private static int PORT;
    private static Activity a;

    private static boolean isClosed = false;

    private static int command = -1;
    private static HashMap<String, Object> writeData = null;

    public Client(String ip, int port, Activity a)
    {
        IP = ip;
        PORT = port;
        this.a = a;
    }

    @Override
    public void run()
    {
        super.run();

        while (client == null)
        {
            try
            {
                client = new Socket(IP, PORT);
                is = new ObjectInputStream(client.getInputStream());
                os = new ObjectOutputStream(client.getOutputStream());
                Log.i("Connecting", "1");
            } catch (IOException e)
            {
                //e.printStackTrace();
            }
        }

        command = -1;

        toast("Connected to the server", Toast.LENGTH_SHORT);

        while (!isClosed)
        {
            try
            {
                if(command > -1)
                {
                    // writing to the server what to do
                    os.writeInt(command);
                    os.flush();

                    Protocol g = Protocol.values()[command];
                    command = -1;

                    switch (g)
                    {
                        case Register:
                            // writing to the server the username and password
                            os.writeObject(writeData.get("username"));
                            os.flush();
                            os.writeObject(writeData.get("password"));
                            os.flush();

                            // getting if the user info is ok
                            String response = (String) is.readObject();

                            if (response.equals("false"))
                                toast("Username is already taken.", Toast.LENGTH_SHORT);
                            else
                            {
                                toast("Registered successfully.", Toast.LENGTH_SHORT);
                                ((Activity) writeData.get("register")).finish();
                            }
                            break;

                        case Login:
                            // writing to the server the username and password
                            os.writeObject(writeData.get("username"));
                            os.flush();
                            os.writeObject(writeData.get("password"));
                            os.flush();

                            // getting if the user info is ok
                            response = (String) is.readObject();

                            if (response.equals("false"))
                                toast("Incorrect username or password.", Toast.LENGTH_SHORT);
                            else
                            {
                                toast("Login successfully.", Toast.LENGTH_SHORT);
                                command = 2;
                            }
                            break;

                        case ReceiveUserData:
                            // receiving the user data and starting activity
                            ArrayList<UserItem> userData = (ArrayList<UserItem>) is.readObject();
                            startUserDataActivity(userData);
                            break;

                        case AddUserItem:
                            // writing the to sever new item to add
                            os.writeObject(writeData.get("title"));
                            os.flush();
                            os.writeObject(writeData.get("url"));
                            os.flush();
                            break;

                        case RemoveUserItem:
                            // writing to the server item to delete
                            os.writeObject(writeData.get("remove"));
                            os.flush();
                            break;

                        case Close:
                            // telling to the server to close the socket
                            isClosed = true;
                            is.close();
                            os.close();
                            client.close();
                            break;
                    }
                }

            } catch (IOException | ClassNotFoundException e)
            {
                command = -1;
                e.printStackTrace();
            }
        }
    }

    // getting commands from other activities
    public void setCommand(int c)
    {
        command = c;
    }

    // getting commands from other activities with data if needed
    public void setCommand(int c, HashMap<String, Object> writeData)
    {
        this.writeData = writeData;
        command = c;
    }

    // creating a toast
    // this needed because this implemented not from the main thread
    private void toast(final String massage, final int time)
    {
        a.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(a, massage, time).show();
            }
        });
    }

    // starting the User Data activity
    private void startUserDataActivity(final ArrayList<UserItem> t)
    {
        a.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i = new Intent(a, UserDataActivity.class);
                // putting in the intent the user data
                i.putExtra("userData", t);
                a.startActivity(i);
                a.overridePendingTransition(R.anim.spin_anim, R.anim.static_anim);
            }
        });
    }
}
