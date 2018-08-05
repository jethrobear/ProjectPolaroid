package edu.fcpc.polaroid.wifi;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketCache {
    private static InetAddress activeHost = null;

    public static InetAddress getActiveHost(WifiManager wifiManager) throws NoServerFoundException, UnknownHostException {
        boolean activeHostIsReachable = false;

        // Check if the active host is still open
        try {
            if(activeHost != null) {
                activeHostIsReachable = activeHost.isReachable(100);
                Socket socket = new Socket(activeHost.getHostAddress(), 1234);
                socket.close();
                Log.i("PP", String.format("Host %s is reachable", activeHost.getHostAddress()));
            }
        } catch (IOException ioe) {
            activeHostIsReachable = false;
            Log.i("PP", "Host had expired, retrying to find hosts");
        }

        // Retrieve new set of active host if the current host is down
        if (!activeHostIsReachable) {
            int subnet = wifiManager.getDhcpInfo().gateway;
            byte[] ipBytes = new byte[]{(byte) (subnet & 0xff), (byte) (subnet >> 8 & 0xff), (byte) (subnet >> 16 & 0xff), 0x00};

            // Loop into all the /24 address
            for (int i = 1; i <= 254; i++) {
                ipBytes[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ipBytes);
                Log.i("PP", String.format("Pinging %s", address.getHostAddress()));
                try {
                    // Use the address ONLY IF the
                    if (address.isReachable(10)) {
                        Socket socket = new Socket(address.getHostAddress(), 1234);
                        socket.close();
                        Log.i("PP", String.format("Address %s has reachable port 1234, saving this to cache", address.getHostAddress()));
                        activeHost = address;
                        return activeHost;
                    }
                } catch (IOException ioe) {
                    // Move to the next host
                    Log.i("PP", String.format("Address %s has no reachable port 1234, polling more", address.getHostAddress()));
                    continue;
                }
            }


            Log.i("PP", "No address found in subnet");
            throw new NoServerFoundException();
        } else
            return activeHost;
    }

    static class NoServerFoundException extends Exception {

    }
}
