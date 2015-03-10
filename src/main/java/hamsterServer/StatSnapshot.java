package hamsterServer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Wyvern on 10.03.2015.
 */
public class StatSnapshot {


    private AtomicInteger totalConnections;
    private AtomicInteger openConnection;
    private HashMap<String, Long[]> differentIpRequests;
    private HashMap<String, HashSet<String>> uniqueIpRequests;
    private Map<String, Integer> urlRequests = new TreeMap<>();
    private StatisticForOneConnection[] lastConnections;


    public StatSnapshot(StatisticForOneConnection[] lastConnections,
                        AtomicInteger totalConnections,
                        AtomicInteger openConnection,
                        HashMap<String, Long[]> differentIpRequests,
                        HashMap<String, HashSet<String>> uniqueIpRequests,
                        Map<String, Integer> urlRequests) {

        this.lastConnections = lastConnections;
        this.totalConnections = totalConnections;
        this.openConnection = openConnection;
        this.differentIpRequests = differentIpRequests;
        this.uniqueIpRequests = uniqueIpRequests;
        this.urlRequests = urlRequests;
    }

    public AtomicInteger getTotalConnections() {
        return totalConnections;
    }

    public AtomicInteger getOpenConnection() {
        return openConnection;
    }

    public HashMap<String, Long[]> getDifferentIpRequests() {
        return differentIpRequests;
    }

    public HashMap<String, HashSet<String>> getUniqueIpRequests() {
        return uniqueIpRequests;
    }

    public Map<String, Integer> getUrlRequests() {
        return urlRequests;
    }

    public StatisticForOneConnection[] getLastConnections() {
        return lastConnections;
    }
}








