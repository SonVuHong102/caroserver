/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {
	
	private HashMap<String,Runnable> userMap;
	
	public void startServer() {
		ServerSocket server = null;
		try {
			//Create TCP Socket Server
			server = new ServerSocket(Value.serverPort);
			
			userMap = new HashMap<String,Runnable>();
			// Listening for client
			System.out.println("$ Server Created");
			while(true) {
				Socket client = server.accept();
				String name = "client " + Value.clientOrdinalNum;
				Value.clientOrdinalNum++;
				System.out.println("\nClient : " + client.getInetAddress().getHostAddress() + " - " + client.getPort() + " connected as " + name);
				ClientListener newClient = new ClientListener(client,name);
				newClient.start();
				userMap.put(name, newClient);
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		// TODO On Server Stop  : ¯\_(ツ)_/¯
	}
	
	
	
	private class ClientListener implements Runnable {
		
		private Socket client;
		private String clientName;
		private DataInputStream fromClient;
		private DataOutputStream toClient;
		
		public ClientListener(Socket client,String clientName) throws IOException {
			this.client = client;
			this.clientName = clientName;
			fromClient = new DataInputStream(client.getInputStream());
			toClient = new DataOutputStream(client.getOutputStream());
		}
		private Thread worker;
	    private final AtomicBoolean running = new AtomicBoolean(false);
		
	    public void start() {
	        worker = new Thread(this);
	        worker.start();
	    }
	    
		public void stop() {
	        running.set(false);
	    }
		
		private void sendToClient(String msg) {
			try {
				toClient.writeUTF(msg);
				System.out.println("Send to Client : " + msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			running.set(true);
			while(running.get()) {
				try {
					String msg = fromClient.readUTF();
					String[] t = msg.split(" ");
					if(t[0].equals("ClosingSocket")) {
						stop();
						fromClient.close();
						toClient.close();
						client.close();
						System.out.println("$ " + clientName + " has disconnected");
					} else if(t[0].equals("Login")) {
						UserDAO userDb = new UserDAO();
						String username = t[1];
						String password = t[2];
						if(userDb.checkLogin(username, password)) {
							sendToClient("Login Accepted");
						} else {
							sendToClient("Login Rejected");
						}
					} else if(t[0].equals("Signup")) {
						String username = t[1];
						String password = t[2];
						String repassword = t[3];
						String name = t[4];
						if(!password.equals(repassword)) {
							sendToClient("Signup " + "PasswordNotMatch");
						} else {
							UserDAO userDb = new UserDAO();
							if(userDb.isExistedUsername(username)) {
								sendToClient("Signup " + "UsernameIsExisted");
							} else if(userDb.addUser(new User(username,password,name,0))) {
								sendToClient("Signup " + "Successed");
							} else {
								sendToClient("Signup " + "Failed");
							}
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					if(e.getMessage().equals("Connection reset")) {
						stop();
					}
				}
			}
		}
	}
}
