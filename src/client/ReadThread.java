package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import javafx.application.Platform;

/*
 * READTHREAD CLASS:
 * 
 * When a Client is created through a instance of ChatAppUI
 * a ReadThread is created to handle all incoming traffic to 
 * the Client from the Server on a new thread
 * 
 * The ReadThread listens for messages sent from the Server
 * and performs actions when a command message is recieved
 * 
 * Actions Include:
 * -maintaining members list (new users/user logout)
 * -recieving direct messages
 * -recieving regular messages
 */

public class ReadThread extends Thread {
	private String response;
	private BufferedReader reader;
	private Socket socket;
	private Client client;
	
	
	public ReadThread(Socket socket, Client client)
	{
		this.socket = socket;
		this.client = client;
		
		try
		{
			InputStream input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while(true)
		{		
			try {
				
				//listening
				response = reader.readLine();
				
				
				//SCANNING NEW MESSAGES
				
				
				/*
				 * new Client joined check:
				 * 
				 * when a new Client joins, the Server broadcasts
				 * a new Client command message "@username"
				 * 
				 * the '@' is removed and added to this Client's username
				 * list
				 */
				if(response.contains("@"))
				{
					response = response.substring(1); 
					if(!client.getUsernames().contains(response))
					{
						client.getUsernames().add(response);
					}					
				}
				/*
				 * direct message check:
				 * 
				 * when a Server sends this Client a direct message from
				 * another Client it is in the format "sender&recipient&message"
				 *  
				 * when the command message is recieved it is broken at '&' into 
				 * an array of strings
				 * 
				 * using this array, a new dialog is prompted on this Client's
				 * ChatAppUI displaying the direct message
				 */
				else if(response.contains("&"))				
				{
					String[] splitDirectMessage = response.split("&", 3);
					Platform.runLater(()->{
						client.getGUI().alertText("Message from " + splitDirectMessage[0], splitDirectMessage[2]);
				    });			
				}
				/*
				 * Client logged check:
				 * 
				 * when a Client logs out, the Server broadcasts 
				 * a Client logout command message "~username"
				 * 
				 * the '~' is removed and the logged Client  is removed
				 * from this Client's username list
				 */
				else if(response.contains("~"))
				{
					response = response.substring(1);
					client.getUsernames().remove(response);
				}
				/*
				 * default action:
				 * 
				 * add regular message to this Client's ChatAppUI
				 */
				else
				{
					client.addMessage(response);
				}

				//END SCANNING
				
				/*
				 * NOTE on next few lines:
				 * 
				 * in order to update lists on the fly in the UI, the ReadThread
				 * must execute methods belonging to the Client's ChatAppUI instance
				 * 
				 * calling these methods directly from this regular thread would cause
				 * a invalid state exception because they are JavaFX() methods from ChatAppUI
				 * that need to be executed on a JavaFX thread
				 * 
				 * the method runLater() is used to execute the lines of code on the ChatAppUI 
				 * JavaFX thread at a later time
				 */
				Platform.runLater(()->{
			        client.getGUI().setChat();
			        client.getGUI().setMemebers();
			    });
				
					
			} 
			catch (Exception e) 
			{
				try {
					/*
					 * ReadThread socket is closed when owning Client's
					 * socket is closed and null pointer is thrown
					 */
					socket.close();
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
				break;
			}
		}
	}	
}
