package edu.fcpc.polaroid;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class ServerMeshWatchdog implements Runnable {
    private JmDNS jmdns;
    public int serverCount = -1;
    private InetAddress localAddress;
    private int localPort;

    public ServerMeshWatchdog(InetAddress inetAddress, int port) {
        localAddress = inetAddress;
        localPort = port;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            jmdns = JmDNS.create();

            // Loop through all the machines in the sub-subnet
            for (; ; ) {
                List<ServiceInfo> providers = Arrays.asList(jmdns.list("_http._tcp.local."));
                ArrayList<String> providersTimestamps = new ArrayList<>();
                for (ServiceInfo info : providers)
                    providersTimestamps.add(new String(info.getTextBytes()));
                Collections.sort(providersTimestamps);

                // Determine local provider's instance in `providers`
                ServiceInfo localProvider = null;
                for (ServiceInfo info : providers) {
                    if (Arrays.asList(info.getHostAddresses()).contains(localAddress.getHostAddress()) &&
                            info.getPort() == localPort) {
                        localProvider = info;
                        break;
                    }
                }

                // Assign the server's number via sorted array
                int newServerCount;
                if (localProvider != null)
                    newServerCount = providersTimestamps.indexOf(new String(localProvider.getTextBytes()));
                else
                    newServerCount = -1;
                if (newServerCount != serverCount) {
                    serverCount = newServerCount;
                    LoggerFactory.getLogger(ServerMeshWatchdog.class).info(String.format("Server assigned as #%d", serverCount));
                }

                // TODO: Send existing images here as well
                File path = new File("Pictures");
                path.mkdirs();
                for (ServiceInfo info : providers)
                    for (File pathImages : path.listFiles())
                        if (FilenameUtils.getExtension(pathImages.getAbsolutePath()).equals("png")) {
                            Socket imageSendSocket = new Socket(info.getInetAddresses()[0], info.getPort());
                            SentPackage sentPackage = new SentPackage();
                            sentPackage.packageStatus = PackageStatus.PICTURE;
                            sentPackage.filename = pathImages.getName();
                            sentPackage.imagebinary = Files.readAllBytes(pathImages.toPath());
                            ObjectOutputStream objOutStream = new ObjectOutputStream(new BufferedOutputStream(imageSendSocket.getOutputStream()));
                            objOutStream.writeObject(sentPackage);
                            objOutStream.flush();
                            objOutStream.close();

                            // Read response
                            ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(imageSendSocket.getInputStream()));
                            SentPackage recvPackage = (SentPackage) objInStream.readObject();
                            LoggerFactory.getLogger(ServerMeshWatchdog.class).info(recvPackage.packageStatus.toString());
                            objInStream.close();
                            imageSendSocket.close();
                        }
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO: Check issue here
            LoggerFactory.getLogger(ServerMeshWatchdog.class).error(e.getMessage());
        }
    }
}
