package edu.fcpc.polaroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;
import javafx.scene.image.Image;

public class WiFiHelper implements Runnable {
	private Main main;
	private ServerSocket serverSocket;

	public WiFiHelper(Main main) {
		this.main = main;
	}

	@Override
	public void run() {
		// Open the server
		try {
			serverSocket = new ServerSocket(1234);
		} catch (IOException ioe) {
			System.exit(-1);
		}
		
		// Try to read something from it
		for(;;) {
			try {
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
					// Login the member
					String username = sentPackage.username;
					String password = sentPackage.password;
					boolean hasLogin = SQLHelper.loginUser(username, password);
					
					// Send the response to the client
					SentPackage returnPackage = new SentPackage();
					if(hasLogin)
						returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_OK;
					else
						returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_FAIL;
					ByteArrayOutputStream byteArrOutStream = new ByteArrayOutputStream();
					ObjectOutputStream objOutStream = new ObjectOutputStream(byteArrOutStream);
					objOutStream.writeObject(returnPackage);
					objOutStream.flush();
					objOutStream.close();
					DataOutputStream dataOutStream = new DataOutputStream(socket.getOutputStream());
					dataOutStream.write(byteArrOutStream.toByteArray());
					dataOutStream.flush();
					dataOutStream.close();
					
				}else if(sentPackage.packageStatus == PackageStatus.REGISTER) {
					// TODO: Register the member
				}
			}catch(IOException ioe) {
				// Retry
				continue;
			}catch(ClassNotFoundException cnfe) {
				// INFO: This should not happen
			}
		}
	}

}
