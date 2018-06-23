package com.ofek.urldatabase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<UserItem>
{
    private ArrayList<UserItem> items;

    public CustomAdapter(@NonNull Context context, @NonNull ArrayList<UserItem> items)
    {
        super(context, 0, items);
        this.items = items;
    }

    // setting up the ListView item layout
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        UserItem ui = getItem(position);

        // if the view doesn't have layout creating it(inflating);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_layout, parent, false);

        // setting the TextViews text
        TextView title = (TextView) convertView.findViewById(R.id.listview_item_title);
        TextView url = (TextView) convertView.findViewById(R.id.listview_item_url);

        title.setText(ui.getTitle());
        url.setText(ui.getURL());

        return convertView;
    }

    public ArrayList<UserItem> getItems()
    {
        return items;
    }
}