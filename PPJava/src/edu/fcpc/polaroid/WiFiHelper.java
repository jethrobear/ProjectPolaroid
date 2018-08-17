package edu.fcpc.polaroid;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;
import javafx.scene.image.Image;

public class WiFiHelper implements Runnable {
    private Main main;
    private ServerSocket serverSocketAndroid, serverSocketJava;
    private Logger logger;
	private ServiceInfo serviceInfoAndroid, serviceInfoJava;
	private ArrayList<MeshNodeInfo> meshNetwork = new ArrayList<MeshNodeInfo>();
	private JmDNS jmdns;
	
    public WiFiHelper(Main main) {
        this.main = main;
        logger = LoggerFactory.getLogger(WiFiHelper.class);
        
        // Register the service on mDNS
 		try {
 	        jmdns = JmDNS.create();
 	        serviceInfoAndroid = ServiceInfo.create("_http._tcp.local", "example", 1234, "path=index.html");
 	        jmdns.registerService(serviceInfoAndroid);
 	        serverSocketJava = new ServerSocket(0);
 	        serviceInfoJava = ServiceInfo.create("_workspace._tcp.local", UUID.randomUUID().toString(), serverSocketJava.getLocalPort(), "");
 	        jmdns.registerService(serviceInfoJava);
 	        jmdns.addServiceListener("_workspace._tcp.local.", new ServiceListener() {
 				
 				@Override
 				public void serviceResolved(ServiceEvent arg0) {
 					MeshNodeInfo info = new MeshNodeInfo(arg0);
 					if(info.inetAddress != null) {
	 					logger.info(String.format("Mesh node resolved: %s:%d", info.inetAddress.getHostAddress(), info.port));
	 					meshNetwork.add(info);
	 					logger.info(String.valueOf(meshNetwork.size()));
 					}
 				}
 				
 				@Override
 				public void serviceRemoved(ServiceEvent arg0) {
 					// TODO: Assume that we want leaking
 				}
 				
 				@Override
 				public void serviceAdded(ServiceEvent arg0) {}
 			});
 		}catch(BindException be) {
 			logger.error(be.getMessage());
 			System.exit(-1);
 		}catch(IOException ioe) {
 			logger.warn(ioe.getMessage());
 		}     		
    }

    @Override
    public void run() {
        try {
            // Open the server
            serverSocketAndroid = new ServerSocket(serviceInfoAndroid.getPort());
            logger.info(String.format("Initialised server connection as %s:%d", 
            		                  serverSocketAndroid.getInetAddress().getHostAddress(),
            		                  serverSocketAndroid.getLocalPort()));
        } catch(BindException be) {
        	// Possible that the server is already been opened, so try to setup client instead
        	logger.warn(be.getMessage());
        	logger.warn("Setting up clients");
        } catch (IOException ioe) {
        	logger.error(ioe.getMessage());
            System.exit(-1);
        }

        // Try to read something from it
        for (;;) {
            try {
                // Accept incoming connections
            	Socket socket = null;
            	SentPackage sentPackage = null;
            	SentPackage returnPackage = new SentPackage();
            	try {
            		socket = serverSocketAndroid.accept();
            		
            		// Read incoming bytes (Originating from Android)
                    ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                    sentPackage = (SentPackage) objInStream.readObject();
                    returnPackage = new SentPackage();
            		
                    // Send back to the clients
                    for(MeshNodeInfo info : meshNetwork) {
                    	Socket clientSocket = new Socket(info.inetAddress, info.port);
                    	ObjectOutputStream clientObjOutStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    	clientObjOutStream.writeObject(sentPackage);
                    	clientObjOutStream.flush();
                    	clientObjOutStream.close();
                    	clientSocket.close();
                    }
            	}catch(NullPointerException npe) {
            		logger.warn("Assuming client run");
            		for(ServiceInfo info : jmdns.list("_workspace._tcp.local")) {
            			try {
            				MeshNodeInfo meshInfo = new MeshNodeInfo(info);
            				if(meshInfo.inetAddress != null)
            					socket = new Socket(meshInfo.inetAddress, meshInfo.port);
            				else
            					continue;
            				socket.setSoTimeout(1000);
            				ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            sentPackage = (SentPackage) objInStream.readObject();
                            returnPackage = new SentPackage();
            			}catch(IOException e) {
            				// Not a server connection
            				continue;
            			}
            		}
            	}
            
        		

                // Process the data sent
                if (sentPackage.packageStatus == PackageStatus.PICTURE) {
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

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sentPackage.imagebinary);
                        Image image = new Image(byteArrayInputStream);
                        main.setImage(image);
                        returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_OK;
                    } catch (IOException ioe) {
                    	logger.warn(ioe.getMessage());
                        returnPackage.packageStatus = PackageStatus.PICTURE_RESPONSE_FAIL;
                        returnPackage.retMessage = ioe.getMessage();
                    }

                } else if (sentPackage.packageStatus == PackageStatus.LOGIN) {
                    // Login the member
                	logger.info("Received login packet");
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
                	logger.info("Received register packet");
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
            	logger.warn(ioe.getMessage());
                continue;
            } catch (ClassNotFoundException cnfe) {
                // INFO: This should not happen
            	logger.error(cnfe.getMessage());
            	System.exit(-1);
            }
        }
    }

    class MeshNodeInfo{
    	public InetAddress inetAddress;
    	public int port;
    	
    	public MeshNodeInfo(ServiceEvent event) {
    		if(event.getInfo().getHostAddresses().length > 0) {
    			// Assign the InetAddress based on the host address
    			try {
    				inetAddress = InetAddress.getByName(event.getInfo().getHostAddresses()[0]);
    			}catch(UnknownHostException uhe) {
    				inetAddress = null;
    			}
    			
    			// Assign port
    			port = event.getInfo().getPort();
    		}
    	}
    	
    	public MeshNodeInfo(ServiceInfo info) {
    		if(info.getHostAddresses().length > 0) {
    			// Assign the InetAddress based on the host address
    			try {
    				inetAddress = InetAddress.getByName(info.getHostAddresses()[0]);
    			}catch(UnknownHostException uhe) {
    				inetAddress = null;
    			}
    			
    			// Assign port
    			port = info.getPort();
    		}
    	}
    }
}
