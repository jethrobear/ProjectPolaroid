package edu.fcpc.polaroid.wifi;

import android.net.wifi.WifiManager;

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
            if(activeHost != null)
                activeHostIsReachable = activeHost.isReachable(100);
        } catch (IOException ioe) {
            activeHostIsReachable = false;
        }

        // Retrieve new set of active host if the current host is down
        if (!activeHostIsReachable) {
            int subnet = wifiManager.getDhcpInfo().gateway;
            byte[] ipBytes = new byte[]{(byte) (subnet & 0xff), (byte) (subnet >> 8 & 0xff), (byte) (subnet >> 16 & 0xff), 0x00};

            // Loop into all the /24 address
            for (int i = 1; i <= 254; i++) {
                ipBytes[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ipBytes);
                try {
                    // Use the address ONLY IF the
                    if (address.isReachable(100)) {
                        Socket socket = new Socket(address.getHostAddress(), 1234);
                        activeHost = address;
                        return activeHost;
                    }
                } catch (IOException ioe) {
                    // Move to the next host
                    continue;
                }
            }

            throw new NoServerFoundException();
        } else
            return activeHost;
    }

    static class NoServerFoundException extends Exception {

    }
}
