package edu.fcpc.polaroid;

import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class ServerMeshWatchdog implements Runnable {
    private JmDNS jmdns;
    public static HashMap<InetSocketAddress, Integer> registers = new HashMap<>();
    private InetAddress localAddress;

    public ServerMeshWatchdog(InetAddress inetAddress, int port) {
        localAddress = inetAddress;
        registers.put(new InetSocketAddress(inetAddress, port), 0);
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            jmdns = JmDNS.create();

            // Loop through all the machines in the sub-subnet
            for (; ; ) {
                for (ServiceInfo info : jmdns.list("_http._tcp.local.")) {
                    // Exclude sending to itself
                    if(address[0].getAddress().equals(localAddress.getAddress()))

                    LoggerFactory.getLogger(ServerMeshWatchdog.class).info("Sending register entries:");
                    for (Map.Entry<InetSocketAddress, Integer> address : registers.entrySet()) {
                        LoggerFactory.getLogger(ServerMeshWatchdog.class).info(String.format("%d | %s:%d", address.getValue(),
                                address.getKey().getHostName(), address.getKey().getPort()));
                    }
                    Socket socket = new Socket(info.getHostAddresses()[0], info.getPort());
                    socket.setSoTimeout(1000);
                    ObjectOutputStream objOutStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    SentPackage sentPackage = new SentPackage();
                    sentPackage.packageStatus = PackageStatus.SERVER_PING;
                    sentPackage.registers = registers;
                    objOutStream.writeObject(sentPackage);
                    objOutStream.flush();
                    objOutStream.close();
                    socket.close();
                }
            }
        } catch (IOException ioe) {
            // TODO: Check issue here
            LoggerFactory.getLogger(ServerMeshWatchdog.class).error(ioe.getMessage());
        }
    }
}
