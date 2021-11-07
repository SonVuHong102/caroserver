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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {

	private HashMap<String, ClientListener> clientMap;
	private HashMap<String,String> playerMap;

	public void startServer() {
		ServerSocket server = null;
		try {
			// Create TCP Socket Server
			server = new ServerSocket(Value.serverPort);

			clientMap = new HashMap<String, ClientListener>();
			playerMap = new HashMap<String, String>();
			// Listening for client
			System.out.println("$ Server Created");
			while (true) {
				Socket client = server.accept();
				String name = "client " + Value.clientOrdinalNum;
				Value.clientOrdinalNum++;
				System.out.println("\nClient : " + client.getInetAddress().getHostAddress() + " - " + client.getPort()
						+ " connected as " + name);
				ClientListener newClient = new ClientListener(client, name);
				newClient.start();
				clientMap.put(name, newClient);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// TODO On Server Closing : ¯\_(ツ)_/¯
	}
	
// Send Refresh to all
	private void sendRefreshToAll() {
		StringBuilder sb = new StringBuilder("Refresh ");
		String players = String.join(" ", playerMap.values());
		sb.append(players);
		for(ClientListener cl : clientMap.values()) {
			cl.sendRefresh(sb.toString());
		}
	}

	public class ClientListener implements Runnable {

		private Socket client;
		private String clientName;
		private DataInputStream fromClient;
		private DataOutputStream toClient;

		public ClientListener(Socket client, String clientName) throws IOException {
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
			playerMap.remove(clientName);
			clientMap.remove(clientName);
			sendRefreshToAll();
			running.set(false);
		}

		public void sendToClient(String msg) {
			try {
				toClient.writeUTF(msg);
				System.out.println("Send to [" + clientName + "] : " + msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void closeSocket() {
			try {
				fromClient.close();
				toClient.close();
				client.close();
				clientMap.remove(clientName);
				playerMap.remove(clientName);
				sendRefreshToAll();
				this.stop();
				System.out.println("$ " + clientName + " has disconnected");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void checkLogin(String username,String password) {
			ClientDAO userDb = new ClientDAO();
			if (userDb.checkLogin(username, password)) {
				sendToClient("Login Accepted");
				playerMap.put(clientName, username);
			} else {
				sendToClient("Login Rejected");
			}
		}
		
		private void checkSignup(String username,String password,String repassword) {
			if (!password.equals(repassword)) {
				sendToClient("Signup " + "PasswordNotMatch");
			} else {
				ClientDAO userDb = new ClientDAO();
				if (userDb.isExistedUsername(username)) {
					sendToClient("Signup " + "UsernameIsExisted");
				} else if (userDb.addClient(new Client(username, password,0))) {
					sendToClient("Signup " + "Successed");
				} else {
					sendToClient("Signup " + "Failed");
				}
			}
		}
		
		public void sendRefresh(String players) {
			sendToClient(players);
		}
		
		private void sendInvitation(String other) {
			
		}

		public void run() {
			running.set(true);
			while (running.get()) {
				try {
					String msg = fromClient.readUTF();
					String[] t = msg.split(" ");
					if (t[0].equals("ClosingSocket")) {
						closeSocket();
					} else if (t[0].equals("Login")) {
						String username = t[1];
						String password = t[2];
						checkLogin(username,password);
					} else if (t[0].equals("Signup")) {
						String username = t[1];
						String password = t[2];
						String repassword = t[3];
						checkSignup(username, password, repassword);
					} else if(t[0].equals("Refresh")) {
						sendRefreshToAll();
					} else if(t[0].equals("Invite")) {
						sendInvitation(t[1]);
					}

				} catch (Exception e) {
					this.stop();
				}
			}
		}
	}
}
