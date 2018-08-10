package edu.fcpc.polaroid.wifi;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketCache {
    public static InetAddress workingAddress = null;
    public static int workingPort = -1;
}
