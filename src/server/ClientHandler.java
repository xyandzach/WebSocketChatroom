package server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * CLIENTHANDLER CLASS:
 * 
 * When Server accepts new Client a ClientHandler is created to
 * handle messages sent and recieved by the new client on a new thread
 * 
 * The ClientHandler listens for messages sent through the Server and
 * performs actions depending on command messages sent by Clients
 * 
 * if no command is detected, the normal message, sent by this ClientHandler's
 * Client, gets echoed to all other Clients
 */

public class ClientHandler extends Thread {
	private String message;
	private String username;
	private Socket socket;
	private Server server;
	private PrintWriter writer;


	public ClientHandler(Socket socket, Server server)
	{
		this.socket = socket;
		this.server = server;
	}
	
	public void run()
	{
		try
		{

			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
			OutputStream output = socket.getOutputStream();
			writer = new PrintWriter(output, true);
				
			while(true)
			{			
				//listening
				message = reader.readLine();
				
				String[] splitMessage = message.split(":", 2);
				
				//SCANNING NEW MESSAGES
				
				
				/*
				 * Client logout check:
				 * 
				 * when a Client sends a "logout" message to the server, or hits their logout 
				 * button, the Server broadcasts a Client logout command message, "~username",
				 * and a regular message
				 * 
				 * this alerts the other Clients of the logout and to remove
				 * the logged out Client from their members list
				 */
				if(message.equals("logout"))
				{
					server.broadcastMessage("~" + this.username, this);
					server.broadcastMessage("                                           ---" + this.username + " has left the chatroom---", this);
					
					server.getUsernames().remove(this.username);
					break;
				}
				/*
				 * direct message check:
				 * 
				 * when a Client sends a direct message it is formated to 
				 * "sender&recipient&message" , when this command message 
				 * is recieved it is broken at '&' into an array of strings
				 * 
				 * using this array, the recipient Client is found and the 
				 * original direct message command message is sent
				 */
				else if(message.contains("&"))
				{
					String[] splitDirectMessage = message.split("&", 3);
					
					for(ClientHandler x : server.getClients())
					{
						if(x.getUsername().contentEquals(splitDirectMessage[1]))
						{
							x.sendMessage(message);
						}
					}
				}
				/*
				 * new Client check:
				 * 
				 * detects new Client by checking if the name attached to 
				 * message is contained in the Server's list of usernames
				 * 
				 * broadcasts new Client command message so that 
				 * all clients can add the new Client to their
				 * members list
				 */
				else if(!server.getUsernames().contains(splitMessage[0]))
				{
					this.username = splitMessage[0];
					server.getUsernames().add(splitMessage[0]);
					
					for(String x : server.getUsernames())
					{
						this.sendMessage("@" + x);
					}
					
					server.broadcastMessage("                                           ---" + this.username + " has entered the chatroom---", this);					
					server.broadcastMessage("@" + splitMessage[0], this);
				}
				/*
				 * default action:
				 * 
				 * echo regular message back to all Clients
				 */
				else
				{
					server.broadcastMessage(message, this);
				}
			}
			
			
			//closing socket
			socket.close();
			System.out.println("User Disconnected");
		}
		catch (Exception e)
		{		
			e.printStackTrace();
		}
	}
	
	//passed message is sent with this ClientHandler's output writer
	public void sendMessage(String message)
	{
		writer.println(message);
	}
	
	//GETTERS AND SETTERS
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
