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
	
	public Server() {
	}
	
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
				String msg = new String(buf);
				System.out.print("$ Msg received : [" + msg + "]");
				System.out.println(" from : " + p.getAddress() + ":" + p.getPort());
				String responseMsg = analyzer.analyse(msg);
				System.out.println("$ Msg response :  " + responseMsg);
				p = new DatagramPacket(responseMsg.getBytes(), responseMsg.length(), InetAddress.getByName(Value.groupAddress), Value.clientPort);
				server.send(p);
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
