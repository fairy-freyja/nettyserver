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

    // count total number of connections
    private final AtomicInteger totalConnections;

    // current time open connection
    private AtomicInteger openConnection;

    // Kay is String contains IP, and Value is AtomicLong[] contains query_number in
    // first element and last_query_time_in_milliseconds in second.
    private HashMap<String, Long[]> differentIpRequests;

    //Hear Key String contains IP, Value String[] - unique requests,
    // So number unique requests for some IP is String[].length for current IP Kay String
    private HashMap<String, HashSet<String>> uniqueIpRequests;

    // Hear Kay String contains URL name, and Value Integer is number of this url call
    private TreeMap<String, Integer> urlRequests = new TreeMap<>();

    // statistic for last 16 connections
    private LinkedList<StatisticForOneConnection> lastConnections;



    public StatisticData() {
        differentIpRequests = new HashMap<>();
        lastConnections = new LinkedList<>();
        openConnection = new AtomicInteger(0);
        totalConnections = new AtomicInteger(0);
        uniqueIpRequests = new HashMap<>();
    }


    //return statistic data for last connection
    private StatisticForOneConnection getStatisticForLastConnection(ChannelHandlerContext ctx) {
        return ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).get();
    }

    // add last connection statistic data to List lastConnections
    public void addLastConnection(StatisticForOneConnection oneConnection) {
        if (lastConnections.size() > 16) {
            lastConnections.remove(0);
        }
        lastConnections.add(oneConnection);
    }

    public void incrementTotalRequests() {
        totalConnections.incrementAndGet();
    }

    public void incrementOpenConnection() {
        openConnection.incrementAndGet();
    }

    public void decrementOpenConnection() {
        openConnection.decrementAndGet();
    }

    // This method update number_url_call if urlRequests contains last connection URL
    // Or add a new pair of values if it's not.
    synchronized public void updateUrlRequests(StatisticForOneConnection oneConnection) {
        String uri = oneConnection.getURI();

        Integer countRequests = new Integer(1);
        if (urlRequests.containsKey(uri)) {
            countRequests = urlRequests.get(uri) +1;
            urlRequests.put(uri, countRequests);
        } else {
            urlRequests.put(uri, countRequests);
        }

    }

    // in differentIpRequests map Kay is String contains IP, and Value is AtomicLong[] contains query_number in
    // first element and last_query_time_in_milliseconds in second.
    // This method update query_number and last_query_time if  differentIpRequests contains last connection IP
    // And add a new pair of values if it's not.
    synchronized public void updateDifferentIpRequests(ChannelHandlerContext ctx) {
        StatisticForOneConnection oneConnection = ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).get();
        String ip = oneConnection.getIP();
        if (differentIpRequests.containsKey(ip) && differentIpRequests.size()>0) {
            Long[] value = differentIpRequests.get(ip);
            value[0]++;
            value[1] = oneConnection.getDate().getTime();
            differentIpRequests.put(ip, value);
        } else {
            Long numberRequests = new Long(1);
            Long time = oneConnection.getDate().getTime();
            Long[] value = {numberRequests, time};
            differentIpRequests.put(ip, value);
        }
    }


    synchronized public void updateUniqueIpRequests(StatisticForOneConnection oneConnection) {
        // In uniqueIpRequests map Key is String contains IP, Value is String[] - unique requests,
        // So number unique requests for some IP is String[].length for current IP Kay String
        String ip = oneConnection.getIP();
        String uri = oneConnection.getURI();
        HashSet<String> currentValue = new HashSet<>();

        if (uniqueIpRequests.containsKey(ip)) {
            currentValue = uniqueIpRequests.get(ip);
            if (currentValue.contains(uri)) {
                return;
            }
        }
        currentValue.add(uri);
        uniqueIpRequests.put(ip, currentValue);
    }

    // creates temp snapshot that won't be affected by multi-threading and shouldn't be synchronized
    public synchronized StatSnapshot snapshot() {

        // differentIpRequests instances and  uniqueIpRequests instances should be cloned individually,
        // unlike other maps with primitive types

        HashMap<String, Long[]> diffIP = new HashMap<>();
        for (Map.Entry<String, Long[]> item : differentIpRequests.entrySet()) {
            diffIP.put(item.getKey(), Arrays.<Long>copyOf(item.getValue(),(item.getValue()).length));
        }

        HashMap<String, HashSet<String>> uniqIP = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> item : uniqueIpRequests.entrySet()) {
            uniqIP.put(item.getKey(),(HashSet<String>) item.getValue().clone());
        }

        return new StatSnapshot(lastConnections.toArray(new StatisticForOneConnection[lastConnections.size()]),
                new AtomicInteger(totalConnections.get()),
                new AtomicInteger(openConnection.get()),
                diffIP, uniqIP,
                (TreeMap<String, Integer>) urlRequests.clone());

    }



}

