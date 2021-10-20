package client;

import java.util.Optional;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/*
 * CHATAPPUI CLASS:
 * 
 * JavaFX class that extends application to create ChatApp's
 * UI
 * 
 * Two main windows, one for joining and one for the chatroom, 
 * and popups using displayDirectMessageSend() and alertText()
 * 
 * Contains all event handlers for UI that send messages and 
 * commands to Client
 */

public class ChatAppUI extends Application {
	private boolean messageReady = false;
	private boolean directMessageReady = false;
	private String directMessageUsername;
	private String directMessage;
	private ListView<String> chatHistory;
	private ListView<String> membersList;
	private TextField messageTextField;
	private Client client;


	@Override
	public void start(Stage stage) throws Exception 
	{
		Stage primaryStage = stage;
		primaryStage.setResizable(false);
		
		//CONTEXT DROPDOWN MENU
		ContextMenu contextMenu = new ContextMenu();
		MenuItem directMessageItem = new MenuItem("Send Message");
		contextMenu.getItems().add(directMessageItem);
		
		//LOGIN WINDOW SCENE
		Label userNameLabel = new Label("Username: ");
		Label IPAddressLabel = new Label("IP Address: ");
		Label portLabel = new Label("Port: ");
		Button joinButton = new Button("Join");
		
		TextField userNameTextField = new TextField();
		TextField IPAddressTextField = new TextField();
		TextField portTextField = new TextField();
		
		GridPane loginGrid = new GridPane();
		loginGrid.add(userNameLabel, 0, 0);
		loginGrid.add(IPAddressLabel, 0, 1);
		loginGrid.add(portLabel, 0, 2);
		loginGrid.add(joinButton, 0, 3);
		loginGrid.add(userNameTextField, 1, 0);
		loginGrid.add(IPAddressTextField, 1, 1);
		loginGrid.add(portTextField, 1, 2);
		loginGrid.setPadding(new Insets(10,10,10,10));
		
		Scene loginScene = new Scene(loginGrid,235,120);
		
		//CHATROOM WINDOW SCENE
		Label membersLabel = new Label("Members");	
		Label chatroomLabel = new Label("Chatroom");
		Label thisUsernameLabel = new Label();
		membersList = new ListView<String>();	
		chatHistory = new ListView<String>();
		messageTextField = new TextField();
		messageTextField.setPrefWidth(700);
		Button sendButton = new Button("Send");
		Button logoutButton = new Button("Logout");
		logoutButton.setPadding(new Insets(0,1,-1,1));
		
		HBox bottomChatroomBox = new HBox();
		bottomChatroomBox.getChildren().addAll(thisUsernameLabel, messageTextField, sendButton);
		bottomChatroomBox.setPadding(new Insets(0,10,10,10));
		
		VBox leftChatroomVBox = new VBox();
		HBox topOfLeftChatroomVBox = new HBox();
		topOfLeftChatroomVBox.getChildren().addAll(membersLabel);
		topOfLeftChatroomVBox.setSpacing(165);
		leftChatroomVBox.getChildren().addAll(topOfLeftChatroomVBox, membersList);
		leftChatroomVBox.setPadding(new Insets(10,10,10,10));
		
		VBox centerChatroomPane = new VBox();
		HBox topOfCenterChatroomVBox = new HBox();
		topOfCenterChatroomVBox.getChildren().addAll(chatroomLabel, logoutButton);
		centerChatroomPane.getChildren().addAll(topOfCenterChatroomVBox, chatHistory);
		centerChatroomPane.setPadding(new Insets(10,10,10,10));
				
		BorderPane chatroomBorderPane = new BorderPane();
		chatroomBorderPane.setCenter(centerChatroomPane);
		chatroomBorderPane.setLeft(leftChatroomVBox);
		chatroomBorderPane.setBottom(bottomChatroomBox);
		
		Scene chatroomScene = new Scene(chatroomBorderPane);
		
		
		//EVENT HANDLERS
		
		
		/*
		 * right click on membersList event handler:
		 * 
		 * when a user right clicks a member in members list
		 * a contextMenu is shown with directMessage MenuItem
		 */
		membersList.setOnContextMenuRequested(event->
		{
			if(!(membersList.getSelectionModel().getSelectedItem() == null))
			{
				contextMenu.show(membersList, event.getX() + 853, event.getY() + 385);
			}
		});
		
		
		/*
		 * directMessage MenuItem event handler:
		 * 
		 * if user clicks send direct messsage item from contextMenu
		 * a new direct message dialog is displayed with displayDirectMessageSend()
		 * 
		 * directMessageReady boolean tells Client's WriteThread that a direct message
		 * is ready to send
		 */
		directMessageItem.setOnAction(event->
		{
			directMessageUsername = membersList.getSelectionModel().getSelectedItem();
			directMessage = this.displayDirectMessageSend(directMessageUsername);			
			directMessageReady = true;
			membersList.getSelectionModel().clearSelection(membersList.getSelectionModel().getSelectedIndex());
			contextMenu.hide();
		});
			

		
		/*
		 * join button event handler:
		 * 
		 * when a user hits join on the start window a new Client
		 * is created with the information in the start window fields
		 * 
		 * scene is changed to chatroom scene, and server info text is displayed
		 * using alertText() 
		 */
		joinButton.setOnAction(event->
		{
			try 
			{
				client = new Client(userNameTextField.getText(), IPAddressTextField.getText(), Integer.parseInt(portTextField.getText()), this);
				thisUsernameLabel.setText(client.getUserName() + ": ");
				thisUsernameLabel.setPadding(new Insets(4,1,0,3));				
				primaryStage.setScene(chatroomScene);
				primaryStage.setTitle("Chatroom");
				topOfCenterChatroomVBox.setSpacing(chatHistory.getWidth() - (logoutButton.getWidth() * 2 + 15));
				alertText("Chatroom Reserverd Characters", "This chatroom reserves these characters for functionality: \n @ \n ~ \n & \nPlease avoid using these characters in your messages\n\nThank you");
			}
			catch (Exception e)
			{
				alertText("ERROR", "IP Address or Port is invalid.");
				System.out.println(e);
			}
			
			
		});
		
		/*
		 * send button event handler:
		 * 
		 * when user hits send, messageReady boolean is set true,
		 * letting Client WriteThread know that a message is ready,
		 * and the message TextField is cleared
		 */
		sendButton.setOnAction(event->
		{
			setMessageReady(true);
			messageTextField.clear();
		});
		
		
		/*
		 * enter press after typing message event handler:
		 * 
		 * same as send button event but occurs when user
		 * hits enter after typing message
		 */
		messageTextField.setOnKeyPressed(event->{
			if (event.getCode() == KeyCode.ENTER)
			{
				setMessageReady(true);
				messageTextField.clear();
			}
		});
		
		/*
		 * logout button event handler:
		 * 
		 * when user clicks logout, the current Client is
		 * disconnected and start window is reset
		 */
		logoutButton.setOnAction(event->{
			portTextField.clear();
			IPAddressTextField.clear();
			userNameTextField.clear();
			chatHistory.getItems().clear();
			client.setLogout(true);
			primaryStage.setScene(loginScene);
		});
				
		/*
		 * default top right corner close event handler:
		 * 
		 * same as logout button event but occurs when user
		 * hits red close x in top right corner of window
		 */
		primaryStage.setOnCloseRequest(event->
		{
			if(primaryStage.getScene() == loginScene)
			{
				System.exit(0);
			}else {
				portTextField.clear();
				IPAddressTextField.clear();
				userNameTextField.clear();
				chatHistory.getItems().clear();
				client.setLogout(true);
				primaryStage.setScene(loginScene);
			}
			
		});
		
		//END OF EVENT HANDLERS
		
		
		//setting start scene in stage
		primaryStage.setScene(loginScene);
		primaryStage.setTitle("ChatApp");
		primaryStage.show();
		

	}
	
	/*
	 * alertText method:
	 * 
	 * used to create a pop up window with a title
	 * and message
	 */
	public void alertText(String title, String message) 
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.setGraphic(null);
		ButtonType okay = new ButtonType("Okay");
		alert.getButtonTypes().setAll(okay);
		alert.showAndWait();		
	}
	
	/*
	 * displayDirectMessageSend method:
	 * 
	 * creates dialog pop up for direct message, pop up
	 * window contains a title, textfield, and send button
	 */
	public String displayDirectMessageSend(String username) {
		
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Message to " + username);
		dialog.setResizable(false);
		GridPane pane = new GridPane();

		TextField message = new TextField();
		pane.getChildren().add(message);
		
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setPadding(new Insets(20, 35, 20, 35));
		dialog.getDialogPane().setContent(pane);
		
		ButtonType sendButton = new ButtonType("Send", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(sendButton);
		
		Optional<ButtonType> result = dialog.showAndWait();
		
		if (result.isPresent() && result.get() == sendButton)
		{
			directMessageReady = true;
			return message.getText();
		}
		else
		{
			dialog.close();
		}
		
		return "FIX";
		
	}
	
	
	/*
	 * NOT WORKING, alertText() is used instead
	 * displayDirectMessageRecieved method:
	 * 
	 * creates dialog pop up for a recieved direct message, pop up
	 * window contains a title, label, and reply/close button
	 */
//	public String displayDirectMessageRecieved(String username, String message) {
//		
//		// Custom dialog
//		Dialog<ButtonType> dialog = new Dialog<>();
//		dialog.setTitle("Message from " + username);
//		dialog.setResizable(false);
//		GridPane pane = new GridPane();
//	
//		Label messageLabel = new Label(message);
//		pane.getChildren().add(messageLabel);
//		
//		pane.setAlignment(Pos.CENTER);
//		pane.setHgap(10);
//		pane.setVgap(10);
//		pane.setPadding(new Insets(20, 35, 20, 35));
//		dialog.getDialogPane().setContent(pane);
//		
//		ButtonType replyButton = new ButtonType("Reply", ButtonData.OK_DONE);
//		ButtonType closeButton = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
//		dialog.getDialogPane().getButtonTypes().add(replyButton);
//		dialog.getDialogPane().getButtonTypes().add(closeButton);	
//	
//		Optional<ButtonType> result = dialog.showAndWait();
//		
//		if (result.isPresent() && result.get() == replyButton)
//		{
//			directMessage = this.displayDirectMessageSend(username);
//			directMessageReady = true;
//		}
//		else if (result.get() == closeButton)
//		{
//			dialog.close();
//		}
//		
//		return "FIX";
//		
//	}

	//GETTERS AND SETTERS
	public void setMemebers() {
		membersList.getItems().clear();
		membersList.getItems().addAll(client.getUsernames());			
	}
	
	/*
	 * Note on setChat():
	 * 
	 * scrollTo() is used to focus the latest message,
	 * this gives the effect of traditional chatroom
	 * messaging
	 */
	public void setChat() {
		chatHistory.getItems().clear();
		chatHistory.getItems().addAll(client.getHistory());	
		chatHistory.scrollTo(client.getHistory().get(client.getHistory().size() - 2));
	}
	
	public String getMessageFromUI()
	{
		return messageTextField.getText();
	}

	public boolean getMessageReady()
	{
		return this.messageReady;
	}
	
	public void setMessageReady(boolean x)
	{
		messageReady = x;
	}
	
	public boolean getDirectMessageReady() {
		return directMessageReady;
	}

	public void setDirectMessageReady(boolean directMessageReady) {
		this.directMessageReady = directMessageReady;
	}

	public String getDirectMessageUsername() {
		return directMessageUsername;
	}

	public void setDirectMessageUsername(String directMessageUsername) {
		this.directMessageUsername = directMessageUsername;
	}
	
	public String getDirectMessage() {
		return directMessage;
	}

	public void setDirectMessage(String directMessage) {
		this.directMessage = directMessage;
	}
	
	//start ChatAppUI
	public static void main(String args[])
	{
		launch(args);
	}
}
