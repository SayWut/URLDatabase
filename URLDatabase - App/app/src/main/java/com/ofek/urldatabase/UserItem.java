package com.ofek.urldatabase;

import java.io.Serializable;

// utility class that help getting the user data

public class UserItem implements Serializable
{
    private static final long serialVersionUID = -1610497737883487350L;

    private String t;
    private String u;

    public UserItem(String title, String url)
    {
        t = title;
        u = url;
    }

    public String getTitle()
    {
        return t;
    }

    public String getURL()
    {
        return u;
    }
}
