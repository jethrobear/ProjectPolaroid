package edu.fcpc.polaroid;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiHelper implements Runnable{
	private Main main;
	private ServerSocket serverSocket;
	
	public WiFiHelper(Main main) {
		this.main = main;
	}
	
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
			
				// Process the data sent
				if(sentPackage.packageStatus == PackageStatus.PICTURE) {
					// TODO: Add sending salt as well
					
					File path = new File("Pictures");
		            path.mkdirs();
		            File file = new File(path.getAbsolutePath(), new SimpleDateFormat("MMddyyyy_hhmmss").format(new Date()) + ".jpg");
		            FileOutputStream fileOutputStream = new FileOutputStream(file);
		            fileOutputStream.write(sentPackage.imagebinary);
		            fileOutputStream.flush();
		            fileOutputStream.close();
		            
		            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sentPackage.imagebinary);
		            Image image = new Image(byteArrayInputStream);
		            main.setImage(image);
				}else if(sentPackage.packageStatus == PackageStatus.LOGIN) {
					// TODO: DO SOMETHING TO LOGIN
				}else if(sentPackage.packageStatus == PackageStatus.REGISTER) {
					// TODO: Register the member
				}
			}
		}catch(IOException ioe) {
			// TODO: Retry
		}catch(ClassNotFoundException cnfe) {
			// TODO: Join with above
		}
	}

}
