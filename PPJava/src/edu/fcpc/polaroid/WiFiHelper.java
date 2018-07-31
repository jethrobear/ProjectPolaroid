package edu.fcpc.polaroid;

import edu.fcpc.polaroid.SentPackage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WiFiHelper implements Runnable{
	private ServerSocket serverSocket;
	
	@Override
	public void run() {
		try {
			// Open the server
			serverSocket = new ServerSocket(1234);
			
			// Try to read something from it
			for(;;) {
				Socket socket = serverSocket.accept();
				ObjectInputStream objInStream = new ObjectInputStream(socket.getInputStream());
				SentPackage sentPackage = (SentPackage) objInStream.readObject();
			}
		}catch(IOException ioe) {
			// TODO: Retry
		}catch(ClassNotFoundException cnfe) {
			// TODO: Join with above
		}
	}

}
