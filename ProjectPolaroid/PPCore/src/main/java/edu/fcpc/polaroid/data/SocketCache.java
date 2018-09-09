package edu.fcpc.polaroid.data;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.util.HashMap;

public class SocketCache {
    public static HashMap<Integer, ImmutablePair<InetAddress, Integer>> workingAddresses = new HashMap<>();
}
