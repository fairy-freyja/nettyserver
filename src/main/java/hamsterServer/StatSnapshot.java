package hamsterServer;

import java.util.*;

/*
 * Created by Fairy on 10.03.2015.
 */
public class StatSnapshot {
    private long totalConnectionCount;
    private long openConnectionCount;
    private HashMap<String, RemoteIpInfo> ipInfoMap;
    private HashMap<String, Long> requestCountPerUri;
    private StatisticForOneConnection[] lastConnections;
    private HashMap<String, Long> redirections;

    public StatSnapshot(long totalConnectionCount,
                        long openConnectionCount,
                        HashMap<String, RemoteIpInfo> ipInfoMap,
                        HashMap<String, Long> requestCountPerUri,
                        StatisticForOneConnection[] lastConnections,
                        HashMap<String, Long> redirections) {
        this.totalConnectionCount = totalConnectionCount;
        this.openConnectionCount = openConnectionCount;
        this.ipInfoMap = ipInfoMap;
        this.requestCountPerUri = requestCountPerUri;
        this.lastConnections = lastConnections;
        this.redirections = redirections;
    }

    public long getTotalConnectionCount() {
        return totalConnectionCount;
    }

    public long getOpenConnectionCount() {
        return openConnectionCount;
    }

    public HashMap<String, RemoteIpInfo> getIpInfoMap() {
        return ipInfoMap;
    }

    public HashMap<String, Long> getRequestCountPerUri() {
        return requestCountPerUri;
    }

    public StatisticForOneConnection[] getLastConnections() {
        return lastConnections;
    }

    public HashMap<String, Long> getRedirections() {
        return redirections;
    }

    public int getUniqueVisitorCount() {
        return ipInfoMap.size();
    }
}
}








