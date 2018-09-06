package edu.fcpc.polaroid;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

import edu.fcpc.polaroid.helper.MDNSHelper;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class ServerMeshWatchdog implements Runnable {

    public ServerMeshWatchdog() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            // Loop through all the machines in the sub-subnet, so we can send the images
            for (; ; ) {
                File path = new File("Pictures");
                path.mkdirs();
                for (ImmutablePair<InetAddress[], Integer> info : MDNSHelper.getServerSockets())
                    for (File pathImages : path.listFiles())
                        if (FilenameUtils.getExtension(pathImages.getAbsolutePath()).equals("png")) {
                            Socket imageSendSocket = new Socket(info.getLeft()[0], info.getRight());
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
