/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import utils.Value;

/**
 *
 * @author Son Vu
 */
public class Server {
	int portNum;
	
	public Server(int portNum) {
		this.portNum = portNum;
	}
	
	public void start() {
		try {
			System.out.println("$ Creating Server ... ");
			ServerSocket server = new ServerSocket(portNum);
			System.out.println("$ Server Created\n");
			while(true) {
				Socket client = server.accept();
				String name = "client" + Value.clientID;
				Value.clientID++;
				System.out.println("$ Client : " + client.getInetAddress().getHostAddress() + " - " + client.getPort() + " connected as " + name + " \n");
				Thread newClient = new Thread(new SessionServer(client,name));
				newClient.start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
