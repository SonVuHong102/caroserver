/*
 * CODE,
 * CODE NUA,
 * CODE MAI...
 */
package caroserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import utils.Value;

/**
 *
 * @author Son Vu
 */
public class SessionServer implements Runnable {
	private Socket client;
	private String name;
	
	public SessionServer (Socket client,String name) {
		this.client = client;
		this.name = name;
	}
	
	
	public void run() {
		try {
			DataInputStream fromClient = new DataInputStream(client.getInputStream());
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
			
			while(true) {
				toClient.writeUTF(name);
				String msg = fromClient.readUTF();
				if(msg.equalsIgnoreCase("create")) {
					toClient.writeUTF(Value.clientPort + "");
				} else {
					toClient.writeUTF(Value.clientPort + "");
					toClient.writeUTF(Value.hostAddress + "");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
