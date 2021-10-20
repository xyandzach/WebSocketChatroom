package client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/*
 * CLIENT CLASS:
 * 
 * creates object that stores all information relating to a 
 * unique user's ChatAppUI chatroom session (port,logout status, ip, username, chat history, active members, ChatAppUI object)
 * 
 * creates a socket that links the user's session
 * to the server by creating ReadThread and WriteThread threads
 * that manage input/output streams for the socket
 */

public class Client {
	private int port;
	private boolean logout = false;
	private String ip;
	private String userName;
	private String[] filler = new String[16];
	private ArrayList<String> chatHistory = new ArrayList<String>();
	private ArrayList<String> usernames	 = new ArrayList<String>();
	private Socket socket;
	private ChatAppUI gui;

	
	public Client(String userName, String hostname, int port, ChatAppUI gui)
	{
		this.userName = userName;
		this.ip = hostname;
		this.port = port;
		this.gui = gui;
		
		/*
		 * adding filler lines to Client's ChatAppUI message box
		 * to simulate chatroom like messaging
		 */
		for(int i = 0; i < filler.length;i++)
		{
			filler[i] = "";
			chatHistory.add(filler[i]);
		}
		this.run();
	}
	
	/*
	 * run method:
	 * 
	 * called in constructor, creates new socket with information
	 * entered in this Client's ChatAppUI join window
	 * 
	 * creates new instance of ReadThread and WriteThread linked
	 * to this Client
	 */
	public void run()
	{
		try
		{
			socket = new Socket(ip, port);		
			

			new ReadThread(socket, this).start();
			new WriteThread(socket, this).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//adds message to Client's chatHistory list
	public void addMessage(String message)
	{
		chatHistory.add(message);
	}
	
	//adds username to Client's usernames list
	public void addUsername(String username)
	{
		usernames.add(username);
	}
	
	//close Client's socket
	public void disconnect()
	{	
		try {
			this.socket.close();			
		} catch (IOException e) {
			System.out.println("Client socket couldn't disconnect");
			e.printStackTrace();
		}
	}
	
	//checks if Client's usernames list is empty
	public boolean isUsernamesEmpty()
	{
		return usernames.isEmpty();
	}
	
	//GETTERS AND SETTERS
	public String getUserName()
	{
		return this.userName;
	}
	
	public Socket getSocket()
	{
		return this.socket;
	}
	
	public ArrayList<String> getHistory()
	{
		return this.chatHistory;
	}
	
	public ChatAppUI getGUI() {
		return this.gui;
	}
	
	public ArrayList<String> getUsernames()
	{
		return this.usernames;
	}

	public boolean getLogout() {
		return logout;
	}
	
	public void setLogout(boolean logout) {
		this.logout = logout;
	}

}


