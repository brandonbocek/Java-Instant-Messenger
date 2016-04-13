import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
public class Server extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	//constructor
	public Server(){
		super("Brandon's Instant Messenger");
		userText = new JTextField();
		//if you aren't connected, you can't type anything
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());
						//resetting to nothing after message is sent
						userText.setText("");
					}
				}
		);
		add(userText,BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
		
	}
	
	//set up and run the server
	public void startRunning(){
		try{
			//6789 is my made up port number for this app
			//100 is the backlog, how many people can wait at the port
			server = new ServerSocket(6789, 100);
			while(true){
				try{
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException e){
					showMessage("\n Server ended the connection! ");
				}finally{
					closeCrap();
				}
			}
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//wait for connection, then display connection information
	private void waitForConnection() throws IOException{
		showMessage("Waiting for someone to connect...\n");
		//this happens repeatedly until the server connects to the client
		//a socket is only created when the connection is made
		connection = server.accept();
		showMessage(" Now connected to "+ connection.getInetAddress().getHostName());
	}
	//get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup! \n");
	}
	//during the chat conversation
	private void whileChatting() throws IOException{
		String message = " You are now connected! ";
		sendMessage(message);
		ableToType(true);
		do{
			//have a conversation
			try{
				message = (String) input.readObject();
				showMessage("\n"+message);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n idk wtf that user sent!");
			}//ending the chat with END typed in the field
		}while(!message.equals("CLIENT - END"));
	}
	//close streams and sockets after you are done chatting
	private void closeCrap(){
		showMessage("\n Closing connections...\n");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//send message to client
	private void sendMessage(String message){
		try{
			//sending the message to the client
			output.writeObject("SERVER - "+message);
			//gets rid of (sends) any extra bytes for housekeeping
			output.flush();
			//display the message on the screen to see the chat history
			showMessage("\nSERVER - "+message);
			
		}catch(IOException ioException){
			chatWindow.append("\n ERROR: I CANT SEND THAT MESSAGE");
		}
	}
	//updates chat window
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(text);
				}
			}
		);
	}
	//let the user type stuff into their box
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
	
}
