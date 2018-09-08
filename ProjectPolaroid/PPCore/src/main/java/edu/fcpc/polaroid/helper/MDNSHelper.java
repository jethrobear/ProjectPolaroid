package edu.fcpc.polaroid.helper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
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

        String lastServerInstance = "0";
        if (serverNames.size() > 0)
            lastServerInstance = String.valueOf(Collections.max(serverNames) + 1);
        LoggerFactory.getLogger(MDNSHelper.class).info("Requested server index is " + lastServerInstance);
        return lastServerInstance;
    }

    public static void setupMDNSInstance(int port) throws IOException {
        JmDNS jmdns = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", MDNSHelper.getLatestServerInstance(), port, "");
        jmdns.registerService(serviceInfo);
    }

    public static ImmutablePair<InetAddress[], Integer>[] getServerSockets() throws IOException {
        JmDNS jmdns = JmDNS.create();
        ArrayList<ImmutablePair<InetAddress[], Integer>> socketAddresses = new ArrayList<>();
        for (ServiceInfo info : jmdns.list("_http._tcp.local.")) {
            socketAddresses.add(new ImmutablePair(info.getInetAddresses(), info.getPort()));
        }
        return socketAddresses.toArray(new ImmutablePair[]{});
    }
}
