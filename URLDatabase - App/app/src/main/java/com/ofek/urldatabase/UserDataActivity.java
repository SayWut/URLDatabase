package com.ofek.urldatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class UserDataActivity extends AppCompatActivity
{
    private ArrayList<UserItem> userData;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_user_data);

        setTitle("My Urls");

        // extracting the user data from the intent
        userData = (ArrayList<UserItem>) getIntent().getSerializableExtra("userData");
        adapter = new CustomAdapter(this, userData);

        // setting up the floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creating new dialog to add new user item
                final AlertDialog.Builder addItemDialog = new AlertDialog.Builder(UserDataActivity.this);
                addItemDialog.setTitle("Add New Website");
                // inflating the dialog layout
                addItemDialog.setView(getLayoutInflater().inflate(R.layout.dialog_layout, null));
                addItemDialog.setNegativeButton("Cancel", null);
                addItemDialog.setPositiveButton("Add", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EditText eTitle = ((AlertDialog) dialog).findViewById(R.id.dialog_text_title);
                        EditText eUrl = ((AlertDialog) dialog).findViewById(R.id.dialog_text_url);

                        // extracting the title and url from the dialog
                        String title = eTitle.getText().toString().trim();
                        String url = eUrl.getText().toString().trim();

                        // checking if the attributes not empty
                        if (!title.equals("") && !url.equals(""))
                        {
                            // adding the data to hashmap
                            HashMap<String, Object> addItem = new HashMap<String, Object>();
                            addItem.put("title", title);
                            addItem.put("url", url);

                            // adding the data to the adapter
                            // and telling to the listview to refresh
                            adapter.add(new UserItem(title, url));
                            adapter.notifyDataSetChanged();

                            // commanding the server to add new item
                            MainActivity.c.setCommand(3, addItem);
                        } else
                            Toast.makeText(UserDataActivity.this, "Can't add empty fields", Toast.LENGTH_SHORT).show();

                    }
                });

                addItemDialog.show();
            }
        });

        // creating my custom listview
        CustomList dataList = new CustomList(this, adapter);
        dataList.setLayoutParams(new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT));

        // getting the layout and adding the dataList
        CoordinatorLayout c = (CoordinatorLayout) findViewById(R.id.user_data_layout);
        c.addView(dataList, 0);
    }

    @Override
    protected void onDestroy()
    {
        // telling the server that the user disconnected
        MainActivity.c.setCommand(5);
        super.onDestroy();
    }
}
