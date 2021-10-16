/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {
	private ArrayList<Player> playerList;
	
	public Server() {
		playerList = new ArrayList<Player>();
		// TODO TEST BEFORE ADD DATABASE
//		playerList.add(new Player("player1"));
//		playerList.add(new Player("player2"));
//		playerList.add(new Player("player3"));
	}
	
	public void start() {
		try {
			System.out.println("$ Creating Server Multicast ... ");
			DatagramSocket server = new DatagramSocket(null);
//			server.bind(new InetSocketAddress(InetAddress.getByName(Value.serverAddress),Value.serverPort));
			server.bind(new InetSocketAddress(Value.serverPort));
			System.out.println("$ Server Created\n");
			MessageAnalyzer analyzer = new MessageAnalyzer();
			while(true) {
				byte[] buf = new byte[1024];
				DatagramPacket p = new DatagramPacket(buf,buf.length);
				server.receive(p);
				String msg = (new String(buf)).trim();
				System.out.print("$ Msg received : [" + msg + "]");
				System.out.println(" from : " + p.getAddress() + ":" + p.getPort());
				String responseMsg = analyzer.analyze(msg);
				System.out.println("$ Msg responsed :  [" + responseMsg + "]");
				p = new DatagramPacket(responseMsg.getBytes(), responseMsg.length(), InetAddress.getByName(Value.groupAddress), Value.clientPort);
				server.send(p);
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private class MessageAnalyzer {	
		
		public String analyze(String msg) {
			StringBuilder result = new StringBuilder("");
			String[] t = msg.split(" ");
			// Request : [connect] -> Response : [accept connection] (Connection accepted, create new ClientSession)
			if(t[0].equalsIgnoreCase("connect")) {
				result.append("accept connection");
			} // Request : [ <username> <....>] -> Response : [<username> <....>] (Authorized <username> for sending message)
			
			// Request : [<username> login <password>] -> Response : [<username> login accept] (Login accepted, initiate client information)
			else if(t[1].equalsIgnoreCase("login")) {
				String username = t[0];
				String password = t[2]; 
				
				// TODO TEST BEFORE ADD DATABASE
				if(username.equalsIgnoreCase(password)) {
					result.append(username + " login accept");
				} else {
					result.append(username + " login deny");
				}
				
			} 
			// Request : [<username> refresh] -> Response : [<username> refresh <username1> <username2> ...] (Sending Player list)
			else if(t[1].equalsIgnoreCase("refresh")) {
				// TODO add online checking function
				result.append(msg);
				for(Player i : playerList) {
					result.append(" " + i.getUsername());
				}
				
			} 
			// Request : [<username> create] -> Response : [<username> create accept] (Create room for <username>)
			else if(t[1].equalsIgnoreCase("create")) {
				playerList.add(new Player(t[0]));
				result.append(msg + " accept");
			}
			// Request : [<username> invite <opponent>] -> Response : [<username> invite <opponent> sent] (Send invitation from <username> to <opponent>)
			else if(t[1].equalsIgnoreCase("invite")) {
				// TODO check online status
				if(t.length == 3) {
					result.append(msg + " sent");
				} else {
					// Request : [<username> invite <opponent> accepted] -> Response : [<username> invite <opponent> accepted] (invitation accepted. Send creating room response )
					result.append(msg);
				}
			}
			return result.toString();
		}
		
	}
}
