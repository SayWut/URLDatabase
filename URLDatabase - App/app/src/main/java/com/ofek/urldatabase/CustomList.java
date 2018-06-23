package com.ofek.urldatabase;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class CustomList extends ListView implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener
{
    private ArrayList<Integer> selectedPositionItems = new ArrayList<>();
    private CustomAdapter adapter;

    public CustomList(Context context, CustomAdapter adapter)
    {
        super(context);
        this.adapter = adapter;
        setAdapter(adapter);
        setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        setOnItemClickListener(this);
        setMultiChoiceModeListener(this);
    }

    // implementation of AdapterView.OnItemClickListener
    // when the user click on item this method will execute
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // getting the url from the clicked item
        UserItem ui = (UserItem) parent.getItemAtPosition(position);
        String url = ui.getURL();

        // checking if the url have the http start if not adding it
        if (!url.startsWith("https://") && !url.startsWith("http://"))
            url = "http://" + url;

        // displaying the url in th default browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        getContext().startActivity(browserIntent);
    }

    // implementation of AbsListView.MultiChoiceModeListener
    // called when user selecting an item from the ListView
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
    {
        // getting the amount of selected items and displaying
        // it in the menu
        int selected = getCheckedItemCount();
        mode.setTitle(selected + " Selected");

        // adding the selected item position in ArrayList
        if(selectedPositionItems.contains(position))
            selectedPositionItems.remove((Object)position);
        else
            selectedPositionItems.add(position);
    }

    // called when the user do long press on an item
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        // setting the long press menu
        mode.getMenuInflater().inflate(R.menu.listview_selecte_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return false;
    }

    // called when the user pressed on the menu items (not on the ListView)
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Log.i("In", "onActionItemClicked");

        switch (item.getItemId())
        {
            // if the user selected the cancel button
            case R.id.listview_onhold_cancel:
                // closing the menu
                mode.finish();
                break;

            // if the user selected the trashcan
            case R.id.listview_onhold_remove:
                // sorting the selected item in the ArrayList
                Collections.sort(selectedPositionItems);

                // duplicating the ArrayList and sending the copy to the client thread
                // doing so for thread interruption purpose
                ArrayList<Integer> copy = new ArrayList<Integer>();
                copy.addAll(selectedPositionItems);

                HashMap<String, Object> tmp = new HashMap<>();
                tmp.put("remove", copy);
                MainActivity.c.setCommand(4, tmp);

                // removing from the selected items from the ListView
                for(int i = selectedPositionItems.size() - 1; i >= 0; i--)
                    adapter.getItems().remove(selectedPositionItems.get(i).intValue());

                // refreshing the adapter that notifying the ListView
                adapter.notifyDataSetChanged();
                mode.finish();
                break;
        }

        return true;
    }

    // called when the long press menu finished / close
    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        // emptying the selected items ArrayList
        selectedPositionItems.clear();
    }
}
