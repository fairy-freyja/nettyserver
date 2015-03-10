package hamsterServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Created by Fairy on 08.03.15.
 */
public class StatisticData {

    public static final AttributeKey<StatisticForOneConnection> CURRENT_CONNECTION_INFO_KEY =
            AttributeKey.valueOf("humsterserver.CURRENT_CONNECTION_INFO");

    private final int MAX_CONN_INFO_COUNT = 16;

    // count total number of connections
    private long totalConnectionCount;

    // current time open connection
    private long openConnectionCount;

    // Information about IP number of request, last query time, and list uri requested
    private HashMap<String, RemoteIpInfo> ipInfoMap = new HashMap<>();

    // Hear Kay String contains URL name, and Value Integer is number of redirect to this url
    private HashMap<String, Long> requestCountPerUri = new HashMap<>();

    private HashMap<String, Long> redirections = new HashMap<>();

    // statistics for last MAX_CONN_INFO_COUNT connections
    private LinkedList<StatisticForOneConnection> lastConnections = new LinkedList<>();


    public synchronized void registerNewConnection(StatisticForOneConnection oneConnection) {
        addOneConnection(oneConnection);
        openConnectionCount++;
        totalConnectionCount++;
    }

    public synchronized void finishConnection() {
        openConnectionCount--;
    }

    // add new connection statistic data to list, remove old if max number exceeded
    private void addOneConnection(StatisticForOneConnection oneConnection) {
        if (lastConnections.size() >= MAX_CONN_INFO_COUNT) {
            lastConnections.remove(0);
        }
        lastConnections.add(oneConnection);
    }

    // creates temp snapshot that won't be affected by multi-threading and shouldn't be synchronized
    public synchronized void registerRequest(String uri, StatisticForOneConnection connInfo) {
        String ip = connInfo.getIP();
        RemoteIpInfo rii;
        if (ipInfoMap.containsKey(ip))
            rii = ipInfoMap.get(ip);
        else
            ipInfoMap.put(ip, rii = new RemoteIpInfo());

        rii.getUriRequested().add(uri);
        rii.incRequestCount();
        rii.setLastQueryTime(connInfo.getDate().getTime());
    }

    public synchronized void registerRedirectionRequest(String redirectUri) {
        if (redirections.containsKey(redirectUri))
            redirections.put(redirectUri, redirections.get(redirectUri) + 1);
        else
            redirections.put(redirectUri, (long) 1);
    }

    public synchronized StatSnapshot snapshot() {

        // RemoteIpInfo instances should be cloned individually, unlike other maps with primitive types
        HashMap<String, RemoteIpInfo> ipMap = new HashMap<>();
        for (Map.Entry<String, RemoteIpInfo> item : ipInfoMap.entrySet())
            ipMap.put(item.getKey(), (RemoteIpInfo) item.getValue().clone());

        return new StatSnapshot(totalConnectionCount,
                openConnectionCount,
                ipMap,
                (HashMap<String, Long>) requestCountPerUri.clone(),
                lastConnections.toArray(new StatisticForOneConnection[lastConnections.size()]),
                (HashMap<String, Long>) redirections.clone());
    }

}

