package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

import edu.fcpc.polaroid.helper.MDNSHelper;
import edu.fcpc.polaroid.helper.SQLHelper;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiHelper implements Runnable {
    private Main main;
    private ServerSocket serverSocketAndroid;
    private Logger logger = LoggerFactory.getLogger(WiFiHelper.class);
    private InetSocketAddress serverAddress;

    public WiFiHelper(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        // Register the service on mDNS
        try {
            serverSocketAndroid = new ServerSocket(0);
            serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), serverSocketAndroid.getLocalPort());
            new ServerMeshWatchdog(InetAddress.getLocalHost(), serverSocketAndroid.getLocalPort());
            logger.info(String.format(Locale.ENGLISH, "Initialised server connection as %s:%d",
                    serverAddress.getAddress().getHostAddress(),
                    serverAddress.getPort()));
            MDNSHelper.setupMDNSInstance(serverSocketAndroid.getLocalPort());
            main.removeStatus();
        } catch (IOException ioe) {
            logger.warn(ioe.getMessage());
        }

        // Try to read something from it
        for (; ; ) {
            // TODO: Need for-loop here for each individual sockets
            try {
                // Accept incoming connections
                Socket socket = serverSocketAndroid.accept();
                socket.setSoTimeout(1000);

                // Read incoming bytes (Unknown origin)
                ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                SentPackage sentPackage = (SentPackage) objInStream.readObject();
                SentPackage returnPackage = new SentPackage();

                String username = sentPackage.username;
                String password = sentPackage.password;
                String lastname = sentPackage.lastname;
                String firstname = sentPackage.firstname;

                int birthmonth = Integer.MIN_VALUE;
                if (sentPackage.birthmonth != null)
                    birthmonth = Integer.parseInt(sentPackage.birthmonth);
                int birthday = Integer.MIN_VALUE;
                if (sentPackage.birthday != null)
                    birthday = Integer.parseInt(sentPackage.birthday);
                int birthyear = Integer.MIN_VALUE;
                if (sentPackage.birthyear != null)
                    birthyear = Integer.parseInt(sentPackage.birthyear);

                // Process the data sent
                switch (sentPackage.packageStatus) {
                    case PICTURE:
                        // TODO: Add sending salt as well
                        logger.info("Received image packet");
                        try {
                            File path = new File("Pictures");
                            path.mkdirs();
                            File file = new File(path.getAbsolutePath(), sentPackage.filename);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(sentPackage.imagebinary);
                            fileOutputStream.flush();
                            fileOutputStream.close();

                            // TODO: This should be checked first
                            main.setImage(sentPackage.imagebinary);
                            returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_OK;
                        } catch (IOException ioe) {
                            logger.warn(ioe.getMessage());
                            returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_FAIL;
                            returnPackage.retMessage = ioe.getMessage();
                        }
                        break;
                    case LOGIN:
                        // Login the member
                        logger.info("Received login packet");
                        boolean hasLogin = SQLHelper.loginUser(username, password);

                        // Send the response to the client
                        if (hasLogin)
                            returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_OK;
                        else
                            returnPackage.packageStatus = PackageStatus.LOGIN_RESPONSE_FAIL;
                        break;
                    case REGISTER:
                        // Register the member
                        logger.info("Received register packet");

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
                        break;
                    default:
                        break;
                }

                ObjectOutputStream objOutStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                objOutStream.writeObject(returnPackage);
                objOutStream.flush();
                objOutStream.close();
                socket.close();
            } catch (IOException ioe) {
                // Packet received was invalid or severed connection
                logger.warn(ioe.getMessage());
            } catch (ClassNotFoundException cnfe) {
                // INFO: This should not happen
                logger.error(cnfe.getMessage());
                System.exit(-1);
            }
        }
    }
}
