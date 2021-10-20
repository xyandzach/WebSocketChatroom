package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;

/*
 * WRITETHREAD CLASS:
 * 
 * When a Client is created through a instance of ChatAppUI
 * a WriteThread is created to handle all traffic sent by
 * the Client on a new thread
 * 
 * The WriteThread listens for messages sent from the Client's
 * ChatAppUI and performs actions when the UI sends a command message
 * 
 * Actions Include:
 * -sending log in and log out messages for client's session
 * -sending direct messages
 * -sending regular messages
 */

public class WriteThread extends Thread{
    	private String message;
	 	private PrintWriter writer;
	    private Socket socket;
	    private Client client;

	    
	    public WriteThread(Socket socket, Client client) {
	        this.socket = socket;
	        this.client = client;
	        
	        try {
	            OutputStream output = socket.getOutputStream();
	            writer = new PrintWriter(output, true);
	        } catch (IOException ex) {
	            System.out.println("Error getting output stream: " + ex.getMessage());
	            ex.printStackTrace();
	        }
	    }
	    
	    public void run() {
	    	
	        //on first cycle send message to server with new username
	        writer.println(client.getUserName() + ": enter");
	        
	        do {
	        	//listening
	        	message = client.getGUI().getMessageFromUI();
	        	
	        	//SCANNING UI
	        	
	        	/*
	        	 * this Client logout check:
	        	 * 
	        	 * when this user types "logout" or hits logout button,
	        	 * Client.logout boolean is set true
	        	 * 
	        	 * this Client sends "logout" command message to server,
	        	 * alerting other Clients of its logout, and is disconnected
	        	 */
	        	if(client.getLogout() == true)
	        	{
	        		writer.println("logout");
	        		client.disconnect();
	        		break;
	        	}
	        	/*
	        	 * regular message sent check:
	        	 * 
	        	 * when user hits send button or hits enter after
	        	 * typing a message, ChatAppUI.messageReady boolean is 
	        	 * set true
	        	 * 
	        	 * the raw message from the ChatAppUI TextField is formated
	        	 * into a regular message "username: message", then is added to
	        	 * this Clients message list and sent to the server
	        	 */
	        	else if (client.getGUI().getMessageReady() == true)
	        	{

			        String text = client.getUserName() + ": " + message;
			        
			        client.addMessage(text);
			        writer.println(text);
			        
			        //see ReadThread NOTE for more info
			        Platform.runLater(()->{
			        	client.getGUI().setChat();
			        });
			        
			        client.getGUI().setMessageReady(false);
	        	}
	        	/*
	        	 * direct message check:
	        	 * 
	        	 * when user sends direct message, ChatAppUI.directMessageReady
	        	 * boolean is set true
	        	 * 
	        	 * direct message is then formated into a command message 
	        	 * "sender&recipient&message" and sent to server
	        	 */
	        	else if (client.getGUI().getDirectMessageReady() == true)
	        	{
	        		writer.println(client.getUserName()+ "&" + client.getGUI().getDirectMessageUsername() + "&" + client.getGUI().getDirectMessage());
	        		client.getGUI().setDirectMessageReady(false);
	        	}
	        		            
	        	//END SCANNING
		        
	        } while (true);
	 
	        try {
	        	/*
	        	 * socket gets closed when above loop is broken
	        	 */
	            socket.close();
	        } catch (IOException ex) {
	 
	            System.out.println("Error writing to server: " + ex.getMessage());
	        }
	    }
}
