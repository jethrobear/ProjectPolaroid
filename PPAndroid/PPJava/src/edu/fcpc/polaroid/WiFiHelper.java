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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiHelper implements Runnable {
    private Main main;
    private ServerSocket serverSocketAndroid;
    private Logger logger = LoggerFactory.getLogger(WiFiHelper.class);
    private ServiceInfo serviceInfoAndroid;
    private JmDNS jmdns;

    public WiFiHelper(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        // Register the service on mDNS
        try {
            jmdns = JmDNS.create();
            serverSocketAndroid = new ServerSocket(0);
            logger.info(String.format(Locale.ENGLISH, "Initialised server connection as %s:%d",
                    serverSocketAndroid.getInetAddress().getHostAddress(),
                    serverSocketAndroid.getLocalPort()));
            serviceInfoAndroid = ServiceInfo.create("_http._tcp.local.", UUID.randomUUID().toString(),
                    serverSocketAndroid.getLocalPort(), "");
            jmdns.registerService(serviceInfoAndroid);
        } catch (IOException ioe) {
            logger.warn(ioe.getMessage());
        }

        // Try to read something from it
        int serverCount = 0;
        for (; ; ) {
            // TODO: Need for-loop here for each individual sockets
            try {
                // Accept incoming connections
                Socket socket = serverSocketAndroid.accept();

                // Send server ping
                ObjectOutputStream serverObjOutStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                SentPackage serverPackage = new SentPackage();
                serverPackage.packageStatus = PackageStatus.SERVER_PING;
                serverObjOutStream.writeObject(serverPackage);
                serverObjOutStream.flush();

                // Read incoming bytes (Unknown origin)
                ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                SentPackage sentPackage = (SentPackage) objInStream.readObject();
                SentPackage returnPackage = new SentPackage();

                String username = sentPackage.username;
                String password = sentPackage.password;
                String lastname = sentPackage.lastname;
                String firstname = sentPackage.firstname;
                int birthmonth = Integer.parseInt(sentPackage.birthmonth);
                int birthday = Integer.parseInt(sentPackage.birthday);
                int birthyear = Integer.parseInt(sentPackage.birthyear);

                // Process the data sent
                switch (sentPackage.packageStatus) {
                    case PICTURE:
                        // TODO: Add sending salt as well
                        logger.info("Received image packet");
                        try {
                            File path = new File("Pictures");
                            path.mkdirs();
                            File file = new File(path.getAbsolutePath(),
                                    new SimpleDateFormat("MMddyyyy_hhmmss").format(new Date()) + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(sentPackage.imagebinary);
                            fileOutputStream.flush();
                            fileOutputStream.close();

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
                    case SERVER_PING:
                        //DO SOMETHING
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
