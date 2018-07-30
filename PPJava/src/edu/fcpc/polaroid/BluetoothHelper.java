package edu.fcpc.polaroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import javafx.scene.image.Image;

public class BluetoothHelper implements DiscoveryListener, Runnable {
	private Main main;
	private Object lock = new Object();
	private Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
	private Vector<String> vecServices = new Vector<String>();
	
	public BluetoothHelper(Main main) {
		this.main = main;
	}
	
	@Override
	public void run() {
		try{
			main.writeMessage("Turning on Bluetooth");
			
		    LocalDevice localDevice = LocalDevice.getLocalDevice();
		    DiscoveryAgent agent = localDevice.getDiscoveryAgent();
		
		    while(true){
		    	main.writeMessage("Finding devices...");
		    	agent.startInquiry(DiscoveryAgent.GIAC, this);
			
			    try {
			        synchronized (lock) {
			        	lock.wait();
			        }
			    } catch (InterruptedException e) {
			    	main.writeError(e.getMessage());
			    }
			
			    UUID uuids[] = new UUID[1];
			    uuids[0] = new UUID("0000110100001000800000805f9b34fb", false);
			
			    for (RemoteDevice rd : vecDevices) {
			    	try{
				    	main.writeMessage("Device " + rd.getFriendlyName(false) + " found, finding services...");
				    	agent.searchServices(null, uuids, rd, this);
				        try {
				            synchronized (lock) {
				            	lock.wait();
				            }
				        } catch (InterruptedException e) {
				        	main.writeError(e.getMessage());
				        }
			    	}catch(IOException ioe){
			    		main.writeError(ioe.getMessage());
			    	}
			    }
			    
			    if (vecServices.size() != 0){
			    	for (String url : vecServices) {
				        try {
				            StreamConnection conn = (StreamConnection) Connector.open(url, Connector.READ_WRITE);
				            main.writeMessage("Receiving message...");
				            
				            SQLHelper.writeDump("Locking thread...");
				            synchronized (lock) {
				                try {
				                	lock.wait(10);
				                } catch (InterruptedException e) {
				                    main.writeError(e.getMessage());
				                }
				            }
				            
				            SQLHelper.writeDump("Starting to receive files...");
				            // Read all byte
				            DataInputStream dataInputStream = new DataInputStream(conn.openInputStream());
				            DataOutputStream dataOutputStream = new DataOutputStream(conn.openOutputStream());
				            				            
				            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				            byte[] buffer = new byte[1];
				            SQLHelper.writeDump("Calling out to read input stream...");
				            dataInputStream.read(buffer);
				            if(buffer[0] == 0){
					            do{
					            	byteArrayOutputStream.write(buffer, 0, 1);
					            	dataInputStream.read(buffer);
					            }while(buffer[0] >= 0);
				            }else{
				            	int count = 0;
				            	byteArrayOutputStream.write(buffer, 0, 1);
				            	while(-1 != (count = dataInputStream.read(buffer)))
				            		byteArrayOutputStream.write(buffer, 0, count);
				            }
				            SQLHelper.writeDump("Closed connection to input stream");
				            byte btData[] = byteArrayOutputStream.toByteArray();
				            
				            // Determine filetype by checking the magic number 
				            if(String.format("%02x", btData[0]).equals("ff") &&
				               String.format("%02x", btData[1]).equals("d8")){
				            	SQLHelper.writeDump("Writing images...");
				            	File path = new File("Pictures");
					            path.mkdirs();
					            File file = new File(path.getAbsolutePath(), new SimpleDateFormat("MMddyyyy_hhmmss").format(new Date()) + ".jpg");
					            FileOutputStream fileOutputStream = new FileOutputStream(file);
					            fileOutputStream.write(btData);
					            fileOutputStream.flush();
					            fileOutputStream.close();
					            
					            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(btData);
					            Image image = new Image(byteArrayInputStream);
					            main.setImage(image);
				            }else{
				            	SQLHelper.writeDump("Staring logging in...");
				            	// File sent was just text
				            	String str = new String(btData, StandardCharsets.UTF_8);
				            	SQLHelper.writeDump(">>> " + str);
				            	String[] strParams = str.split("\\|");
				            	
				            	if(strParams.length > 1){
				            		// Check stuff
					            	if(strParams[0].contains("login")){
					            		boolean isLogged = SQLHelper.loginUser(strParams[1], strParams[2]);
						            	SQLHelper.writeDump("\tStatus is ..." + isLogged);
						            	// Try to send something back to the phone

						            	if(isLogged){
						            		SQLHelper.writeDump("Sending PASS");
						            		dataOutputStream.writeUTF("PASS");
						            		SQLHelper.writeDump("Sent PASS");
						            	}else{
						            		SQLHelper.writeDump("Sening FAIL");
						            		dataOutputStream.writeUTF("FAIL");
						            		SQLHelper.writeDump("Sent FAIL");
						            	}
						            	dataOutputStream.flush();
						            	
						            	// Write to dump
						            	SQLHelper.writeDump(isLogged ? "Logged in " + str : "Not logged in " + str);
					            	}else if(strParams[0].contains("register")){
					            		String createResult = SQLHelper.createUser(strParams[1], 
					            				strParams[2], 
					            				Integer.parseInt(strParams[3]), 
					            				Integer.parseInt(strParams[4]), 
					            				Integer.parseInt(strParams[5]), 
					            				strParams[6], 
					            				strParams[7]);
					            		if(!"PASS".equals(createResult))
					            			main.writeMessage(createResult);
					            	}
				            	}else{
				            		main.writeMessage("No suitable data received");
				            	}
				            }
				            dataOutputStream.close();
				            dataInputStream.close();
				            conn.close();
				            vecServices = new Vector<String>();
				        }catch (IOException e) {
				        	main.writeError(e.getMessage());
				        }
				    }
			    }else
			    	main.writeMessage("No appropriate service found, trying again.");
		    }
		}catch(IOException ioe){
			main.writeError(ioe.getMessage());
		}
	}

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
	    System.out.println("Device discovered: " + btDevice.getBluetoothAddress());
	    if (!vecDevices.contains(btDevice)) {
	    	vecDevices.addElement(btDevice);
	    }
	}
	
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
	    for (ServiceRecord sr : servRecord) {
	    	vecServices.add(sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
	    }
	}
	
	public void serviceSearchCompleted(int transID, int respCode) {
	    System.out.println("Service search completed - code: " + respCode);
	    synchronized (lock) {
	    	lock.notify();
	    }
	}

	public void inquiryCompleted(int discType) {
	    switch (discType) {
		    case DiscoveryListener.INQUIRY_COMPLETED:
		        System.out.println("INQUIRY_COMPLETED");
		        break;
		
		    case DiscoveryListener.INQUIRY_TERMINATED:
		        System.out.println("INQUIRY_TERMINATED");
		        break;
		
		    case DiscoveryListener.INQUIRY_ERROR:
		        System.out.println("INQUIRY_ERROR");
		        break;
		
		    default:
		        System.out.println("Unknown Response Code");
		        break;
	    }
	    synchronized (lock) {
	    	lock.notify();
	    }
	}
}
