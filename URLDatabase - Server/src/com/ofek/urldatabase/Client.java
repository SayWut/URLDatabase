package com.ofek.urldatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client extends Thread
{
	private static boolean checkingLogin = false;
	private static String mainDataPath = "D:\\Programing Projects\\Eclipse Projects\\URLDatabase\\src\\UsersData";
	private Socket client;
	private ObjectInputStream is;
	private ObjectOutputStream os;

	private String userDataString = null;
	private boolean isLogin = false;
	private String username = null;

	private boolean isClosed = false;

	public Client(Socket c)
	{
		client = c;

		try
		{
			os = new ObjectOutputStream(client.getOutputStream());
			is = new ObjectInputStream(client.getInputStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		super.run();

		while (!isClosed)
		{
			try
			{
				int g = is.readInt();
				System.out.println(g);
				Protocol gate = Protocol.values()[g];

				switch (gate)
				{
				case Register:
					String username = (String) (is.readObject());
					String password = (String) (is.readObject());

					boolean isExists = true;

					if (username != null && password != null && !username.equals("") && !password.equals(""))
						isExists = checkUserExists(username);

					if (isExists)
					{
						os.writeObject("false");
						os.flush();
					} else
					{
						addNewUser(username, password);
						createUserDataFile(username);
						os.writeObject("true");
						os.flush();
					}

					break;

				case Login:
					username = (String) (is.readObject());
					password = (String) (is.readObject());

					boolean isMatch = false;

					if (username != null && password != null && !username.equals("") && !password.equals(""))
					{
						isMatch = checkUserMatch(username, password);
					}

					if (isMatch)
					{
						String path = mainDataPath;
						path += String.format("\\%s\\%s.json", username, username);
						userDataString = getJSONFileData(path);
						isLogin = true;
						this.username = username;

						os.writeObject("true");
						os.flush();
					} else
					{
						os.writeObject("false");
						os.flush();
					}

					break;

				case SendUserData:
					if (isLogin && userDataString != null)
					{
						JSONObject userDataJO = new JSONObject(userDataString);
						JSONArray userURLs = userDataJO.getJSONArray("urls");

						ArrayList<UserItem> userData = new ArrayList<>();

						for (int i = 0; i < userURLs.length(); i++)
						{
							String title = userURLs.getJSONObject(i).getString("title");
							String url = userURLs.getJSONObject(i).getString("url");

							userData.add(new UserItem(title, url));
						}

						os.writeObject(userData);
						os.flush();
					}
					break;

				case AddUserItem:
					if (isLogin && userDataString != null)
					{
						try
						{
							String title = (String) is.readObject();
							String url = (String) is.readObject();

							if (!title.equals("") && !url.equals(""))
							{
								HashMap<String, String> jsonAttributes = new HashMap<>();
								jsonAttributes.put("title", title);
								jsonAttributes.put("url", url);

								JSONObject userDataJO = new JSONObject(userDataString);
								JSONArray userURLs = userDataJO.getJSONArray("urls");
								userURLs.put(jsonAttributes);

								userDataString = userDataJO.toString();
							}
						} catch (ClassNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					break;

				case RemoveUserItem:
					if (isLogin && userDataString != null)
					{
						try
						{
							ArrayList<Integer> remove = (ArrayList<Integer>) is.readObject();
							java.util.Collections.sort(remove);
							
							
							JSONObject userDataJO = new JSONObject(userDataString);
							JSONArray userURLs = userDataJO.getJSONArray("urls");

							for (int i = remove.size() - 1; i >= 0; i--)
								userURLs.remove(remove.get(i));

							userDataString = userDataJO.toString();

						} catch (ClassNotFoundException e)
						{
							e.printStackTrace();
						}
					}

					break;

				case Disconnected:
					clientDisconnect();

					break;

				case Closed:
					isClosed = true;
					break;
				}

			} catch (IOException e)
			{
				isClosed = true;
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}

		try

		{
			clientClosed();
			
			if(isLogin)
				clientDisconnect();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// TODO
	// לשנות שלא בטוח אם הוא מתנתק אז הסוקט נסגר
	// לעשות לזה גורמים נפרדים
	private void clientClosed() throws IOException
	{
		System.out.println("Closed");
		isClosed = true;
		os.close();
		is.close();
		client.close();
	}

	// Adding new user to the database
	private static synchronized void addNewUser(String username, String password) throws JSONException, IOException
	{
		String path = mainDataPath + "\\usersInfo.json";

		JSONObject b = new JSONObject(getJSONFileData(path));
		JSONArray users = b.getJSONArray("users");

		// creating map to declare the json object attributes
		HashMap<String, Object> userData = new HashMap<>();
		userData.put("username", username);
		userData.put("password", password);
		userData.put("logged_in", false);
		userData.put("shared_with_me", new JSONArray());
		
		users.put(userData);

		String formattedJSON = b.toString(2);

		FileWriter fWriter = new FileWriter(new File(path));
		fWriter.write(formattedJSON);
		fWriter.close();
	}

	private void clientDisconnect() throws IOException
	{
		changeKeyValue(username, "logged_in", false);
		
		if (userDataString != null && !userDataString.equals(""))
		{
			String path = mainDataPath;
			path += String.format("\\%s\\%s.json", this.username, this.username);

			FileWriter fw = new FileWriter(new File(path));
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(new JSONObject(userDataString).toString(2));

			bw.close();
		}

		isLogin = false;
		this.username = null;
		userDataString = null;
	}

	private static void createUserDataFile(String username) throws IOException
	{
		String jsonFormat = "{\"urls\":[]}";
		String path = mainDataPath;
		path += String.format("\\%s\\%s.json", username, username);

		File f = new File(path);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		if (!f.exists())
			f.createNewFile();

		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(new JSONObject(jsonFormat).toString(2));

		bw.close();
	}

	// Getting data from json file
	private static String getJSONFileData(String path) throws IOException
	{
		String fileData = "";

		FileReader fReader = new FileReader(new File(path));
		BufferedReader bReader = new BufferedReader(fReader);

		String line;

		while ((line = bReader.readLine()) != null)
			fileData += line;

		fReader.close();
		bReader.close();

		return fileData;
	}

	private static boolean checkUserExists(String username) throws JSONException, IOException
	{
		String path = mainDataPath + "\\usersInfo.json";

		JSONObject b = new JSONObject(getJSONFileData(path));
		JSONArray users = b.getJSONArray("users");

		for (int i = 0; i < users.length(); i++)
		{
			String usernames = users.getJSONObject(i).getString("username");

			if (usernames.equals(username))
				return true;
		}

		return false;
	}

	private static boolean checkUserMatch(String username, String password) throws JSONException, IOException
	{
		String path = mainDataPath + "\\usersInfo.json";

		JSONObject usersInfo = new JSONObject(getJSONFileData(path));
		JSONArray users = usersInfo.getJSONArray("users");

		for (int i = 0; i < users.length(); i++)
		{
			JSONObject user = users.getJSONObject(i);
			
			String usernames = user.getString("username");
			String passwords = user.getString("password");
			boolean isLoggedIn = user.getBoolean("logged_in");

			if (usernames.equals(username) && passwords.equals(password) && !isLoggedIn)
			{	
				changeKeyValue(username, "logged_in", true);
				return true;
			}
			else if (usernames.equals(username))
				return false;
		}

		return false;
	}
	
	private static synchronized void changeKeyValue(String username, String key, Object value) throws IOException
	{
		String path = mainDataPath + "\\usersInfo.json";
		
		JSONObject usersInfo = new JSONObject(getJSONFileData(path));
		JSONArray users = usersInfo.getJSONArray("users");
		
		for (int i = 0; i < users.length(); i++)
		{
			String usernames = users.getJSONObject(i).getString("username");

			if (usernames.equals(username))
				users.getJSONObject(i).put(key, value);
		}
		
		FileWriter fw = new FileWriter(new File(path));
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(usersInfo.toString(2));

		bw.close();
	}
}
