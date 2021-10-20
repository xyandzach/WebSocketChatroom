package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/*
 * SERVER CLASS:
 * 
 * Creates open ServerSocket bound to port 5000 that is
 * constantly listening for new Client connections
 * 
 * When new Client socket is accepted a instance of
 * ClientHandler is created to manage the Client's traffic
 * 
 * Contains broadcastMessage(String message, ClientHandler user), 
 * important  method that sends passed message to all Clients except 
 * the Client (user) that sent it
 */

public class Server {
	private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	private ArrayList<String> usernames = new ArrayList<String>();

	
	public void run() throws IOException{
		
		//attempting to open server
		try(ServerSocket server = new ServerSocket(5000))
		{
			//listening
			System.out.println("Server is listening on port 5000");
			while(true)
			{
				//accepting new clients
				Socket socket = server.accept();
				System.out.println("New User Connected");
				
				//creating instance of ClientHandler for new client connecting
				ClientHandler newUser = new ClientHandler(socket, this);
				clients.add(newUser);
				newUser.start();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*
	 * broadcastMessage method:
	 * 
	 * uses ClientHandler.sendMessage() to send passed message to 
	 * all Clients except the Client passed (user)
	 */
	public void broadcastMessage(String message, ClientHandler user)
	{
		for(ClientHandler x: clients)
		{
			if (x != user)
			{
				x.sendMessage(message);
			}
		}
	}
	
	//GETTERS AND SETTERS
	public ArrayList<String> getUsernames()
	{
		return this.usernames;
	}

	public ArrayList<ClientHandler> getClients() {
		return clients;
	}

	public void setClients(ArrayList<ClientHandler> clients) {
		this.clients = clients;
	}
	
	//start Server
	public static void main(String args[]) throws IOException
	{
		Server server = new Server();
		server.run();
	}

}
