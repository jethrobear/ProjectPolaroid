package edu.fcpc.polaroid;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
        try {
            // Open the server
            serverSocket = new ServerSocket(main.serviceInfo.getPort());
        } catch (IOException ioe) {
            System.exit(-1);
        }

        // Try to read something from it
        for (;;) {
            try {
                // Accept incoming connections
                Socket socket = serverSocket.accept();

                // Read incoming bytes
                ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                SentPackage sentPackage = (SentPackage) objInStream.readObject();
                SentPackage returnPackage = new SentPackage();

                // Process the data sent
                if (sentPackage.packageStatus == PackageStatus.PICTURE) {
                    // TODO: Add sending salt as well

                    try {
                        File path = new File("Pictures");
                        path.mkdirs();
                        File file = new File(path.getAbsolutePath(),
                                new SimpleDateFormat("MMddyyyy_hhmmss").format(new Date()) + ".jpg");
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(sentPackage.imagebinary);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sentPackage.imagebinary);
                        Image image = new Image(byteArrayInputStream);
                        main.setImage(image);
                        returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_OK;
                    } catch (IOException ioe) {
                        returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_FAIL;
                        returnPackage.retMessage = ioe.getMessage();
                    }

                } else if (sentPackage.packageStatus == PackageStatus.LOGIN) {
                    // Login the member
                    String username = sentPackage.username;
                    String password = sentPackage.password;
                    boolean hasLogin = SQLHelper.loginUser(username, password);

                    // Send the response to the client
                    if (hasLogin)
                        returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_OK;
                    else
                        returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_FAIL;
                } else if (sentPackage.packageStatus == PackageStatus.REGISTER) {
                    // Register the member
                    String lastname = sentPackage.lastname;
                    String firstname = sentPackage.firstname;
                    int birthmonth = Integer.parseInt(sentPackage.birthmonth);
                    int birthday = Integer.parseInt(sentPackage.birthday);
                    int birthyear = Integer.parseInt(sentPackage.birthyear);
                    String username = sentPackage.username;
                    String password = sentPackage.password;

                    // Send the response to the client
                    returnPackage.lastname = lastname;
                    returnPackage.firstname = firstname;
                    returnPackage.birthmonth = String.valueOf(birthmonth);
                    returnPackage.birthday = String.valueOf(birthday);
                    returnPackage.birthyear = String.valueOf(birthyear);
                    returnPackage.username = username;
                    returnPackage.password = password;

                    // Check if the member is registered
                    if (SQLHelper.hasUsername(username)) {
                        returnPackage.packageStatus = PackageStatus.REGISTER_RESPONSE_FAIL;
                        returnPackage.retMessage = String.format("Username '%s' already been registered", username);
                    } else {
                        // Create the user
                        String createRetMsg = SQLHelper.createUser(lastname, firstname,
                                                                   birthmonth, birthday, birthyear,
                                                                   username, password);
                        if (createRetMsg.equals("PASS"))
                            returnPackage.packageStatus = PackageStatus.REGISTER_RESPONSE_OK;
                        else
                            returnPackage.packageStatus = PackageStatus.REGISTER_RESPONSE_FAIL;
                        returnPackage.retMessage = createRetMsg;
                    }
                }

                ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());
                objOutStream.writeObject(returnPackage);
                objOutStream.flush();
                objOutStream.close();
                socket.close();
            } catch (IOException ioe) {
                // Retry
                continue;
            } catch (ClassNotFoundException cnfe) {
                // INFO: This should not happen
            }
        }
    }

}
