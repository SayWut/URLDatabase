package com.ofek.urldatabase;

import java.io.IOException;
import java.net.ServerSocket;

public class MainClass
{
	public static void main(String[] args)
	{

		ServerSocket server;
		try
		{
			server = new ServerSocket(3415);

			while (true)
			{
				System.out.println("waiting for client");
				Client c = new Client(server.accept());
				c.start();

				System.out.println("connected...");
			}

		} catch (IOException e)
		{ // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
