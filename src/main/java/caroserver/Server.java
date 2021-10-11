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

import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {
	
	public void start() {
		try {
			System.out.println("$ Creating Server Multicast ... ");
			DatagramSocket server = new DatagramSocket(null);
			server.bind(new InetSocketAddress(InetAddress.getByName(Value.serverAddress),Value.serverPort));
			System.out.println("$ Server Created\n");
			MessageAnalyzer analyzer = new MessageAnalyzer();
			while(true) {
				byte[] buf = new byte[1024];
				DatagramPacket p = new DatagramPacket(buf,buf.length);
				server.receive(p);
				String msg = new String(buf).trim();
				System.out.print("$ Msg received : [" + msg + "]");
				System.out.println(" from : " + p.getAddress() + ":" + p.getPort());
				String responseMsg = analyzer.analyze(msg);
				System.out.println("$ Msg responsed :  " + responseMsg);
				p = new DatagramPacket(responseMsg.getBytes(), responseMsg.length(), InetAddress.getByName(Value.groupAddress), Value.clientPort);
				server.send(p);
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private class MessageAnalyzer {	
		
		public String analyze(String msg) {
			String result = "none";
			String[] t = msg.split(" ");
			// Request : [connect] - Response : [accept connection] (Connection accepted, create new ClientSession)
			if(t[0].equalsIgnoreCase("connect")) {
				result = "accept connection";
			}
			// Request : [login <username> <password>] - Response : [accept <username>] (Login accepted, initiate client information)
			else if(t[0].equalsIgnoreCase("login")) {
				String username = t[1];
				String password = t[2];
				//TEST BEFORE ADD DATABASE
				if(username.equalsIgnoreCase(password)) {
					result = "accept " + username;
				}
			}
			return result;
		}
		
	}
}
