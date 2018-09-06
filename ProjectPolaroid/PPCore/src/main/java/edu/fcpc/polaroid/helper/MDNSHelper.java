package edu.fcpc.polaroid.helper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class MDNSHelper {
    public static String getLatestServerInstance() throws IOException {
        JmDNS jmdns = JmDNS.create();
        ArrayList<Integer> serverNames = new ArrayList<>();
        for (ServiceInfo info : jmdns.list("_http._tcp.local.")) {
            serverNames.add(Integer.valueOf(info.getName()));
        }

        if (serverNames.size() == 0)
            return "0";
        else
            return String.valueOf(Collections.max(serverNames) + 1);
    }

    public static void setupMDNSInstance(int port) throws IOException {
        JmDNS jmdns = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", MDNSHelper.getLatestServerInstance(),
                port, String.valueOf(System.currentTimeMillis() / 1000L));
        jmdns.registerService(serviceInfo);
    }

    public static ImmutablePair<InetAddress[], Integer>[] getServerSockets() throws IOException{
        JmDNS jmdns = JmDNS.create();
        ArrayList<ImmutablePair<InetAddress[], Integer>> socketAddresses = new ArrayList<>();
        for(ServiceInfo info : jmdns.list("_http._tcp.local.")){
            socketAddresses.add(new ImmutablePair(info.getInetAddresses(), info.getPort()));
        }
        return socketAddresses.toArray(new ImmutablePair[]{});
    }
}
