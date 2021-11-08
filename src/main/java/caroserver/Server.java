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

	public void startServer() {
		ServerSocket server = null;
		try {
			// Create TCP Socket Server
			server = new ServerSocket(Value.serverPort);

			clientMap = new HashMap<String, ClientListener>();
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
		StringBuilder sb = new StringBuilder("RefreshPlayer ");
		String players = String.join(" ", clientMap.keySet());
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
				sendRefreshToAll();
				this.stop();
				System.out.println("$ " + clientName + " has disconnected");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void checkLogin(String username,String password) {
			if(clientMap.containsKey(username)) {
				sendToClient("Login InUsing");
				return;
			}
			ClientDAO userDb = new ClientDAO();
			if (userDb.checkLogin(username, password)) {
				sendToClient("Login Accepted");
				clientMap.put(username, this);
				clientMap.remove(clientName);
				clientName = username;
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
			clientMap.get(other).sendToClient("Invitation " + clientName);
			sendToClient("InvitedPlayer " + other);
		}
		
		private void sendRejected(String other) {
			clientMap.get(other).sendToClient("RejectedInvitation " + clientName);
		}
		
		private void sendAccepted(String opp) {
			clientMap.get(opp).sendToClient("AcceptedInvitation " + clientName);
		}
		
		private void sendExitToOpp(String opp) {
			clientMap.get(opp).sendToClient("ExitedGame " + clientName);
		}
		
		private void sendExit(String opp) {
			sendToClient("ExitedGame " + opp);
		}
		
		private void sendMoveToOpp(String opp,String row,String column) {
			clientMap.get(opp).sendToClient("OppMoved " + row + " " + column);
		}

		public void run() {
			running.set(true);
			while (running.get()) {
				try {
					String msg = fromClient.readUTF();
					String[] t = msg.split(" ");
					if (t[0].equals("ClosingSocket")) {
						closeSocket();
						// IN PLAYING GAME
						if(t.length>1) {
							sendExitToOpp(t[1]);
						}
					} else if (t[0].equals("Login")) {
						String username = t[1];
						String password = t[2];
						checkLogin(username,password);
					} else if (t[0].equals("Signup")) {
						String username = t[1];
						String password = t[2];
						String repassword = t[3];
						checkSignup(username, password, repassword);
					} else if(t[0].equals("RefreshPlayer")) {
						sendRefreshToAll();
					} else if(t[0].equals("Invite")) {
						sendInvitation(t[1]);
					} else if(t[0].equals("RejectedInvitation")) {
						sendRejected(t[1]);
					} else if(t[0].equals("AcceptedInvitation")) {
						sendAccepted(t[1]);
					} else if(t[0].equals("ExitedGame")) {
						sendExit(t[1]);
					} else if(t[0].equals("Move")) {
						String opp = t[1];
						String row = t[2];
						String column = t[3];
						sendMoveToOpp(opp,row,column);
					}

				} catch (Exception e) {
					this.stop();
				}
			}
		}
	}
}
