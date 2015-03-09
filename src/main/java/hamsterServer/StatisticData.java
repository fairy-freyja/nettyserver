package hamsterServer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Created by Fairy on 08.03.15.
 */
public class StatisticData {

    // count total number of connections
    private final AtomicInteger totalConnections;

    // current time open connection
    private AtomicInteger openConnection;

    // Kay is String contains IP, and Value is AtomicLong[] contains query_number in
    // first element and last_query_time_in_milliseconds in second.
    private HashMap<String, AtomicLong[]> differentIpRequests;

    //Hear Key String contains IP, Value String[] - unique requests,
    // So number unique requests for some IP is String[].length for current IP Kay String
    private HashMap<String, HashSet<String>> uniqueIpRequests;

    // Hear Kay String contains URL name, and Value Integer is number of this url call
    private Map<String, AtomicInteger> urlRequests = new TreeMap<>();

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
    private StatisticForOneConnection getStatisticForLastConnection() {
        if (lastConnections.size() > 0) {
            return lastConnections.get(lastConnections.size() - 1);
        }
        return null;
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
    synchronized public void updateUrlRequests(String url) {
        AtomicInteger countRequests = new AtomicInteger(1);
        if (urlRequests.containsKey(url)) {
            countRequests = urlRequests.get(url);
            countRequests.incrementAndGet();
            urlRequests.put(url, countRequests);
        } else {
            urlRequests.put(url, countRequests);
        }

    }

    // in differentIpRequests map Kay is String contains IP, and Value is AtomicLong[] contains query_number in
    // first element and last_query_time_in_milliseconds in second.
    // This method update query_number and last_query_time if  differentIpRequests contains last connection IP
    // And add a new pair of values if it's not.
    synchronized public void updateDifferentIpRequests() {
        String ip = getStatisticForLastConnection().getIP();
        if (differentIpRequests.containsKey(ip)) {
            AtomicLong[] value = differentIpRequests.get(ip);
            value[0].incrementAndGet();
            value[1] = new AtomicLong(lastConnections.get(lastConnections.size() - 1).getDate().getTime());
            differentIpRequests.put(ip, value);
        } else {
            AtomicLong numberRequests = new AtomicLong(1);
            AtomicLong time = new AtomicLong(lastConnections.get(lastConnections.size() - 1).getDate().getTime());
            AtomicLong[] value = {numberRequests, time};
            differentIpRequests.put(ip, value);
        }
    }


    synchronized public void updateUniqueIpRequests() {
        // In uniqueIpRequests map Key is String contains IP, Value is String[] - unique requests,
        // So number unique requests for some IP is String[].length for current IP Kay String
        String ip = getStatisticForLastConnection().getIP();
        String uri = getStatisticForLastConnection().getURI();
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


    public synchronized void doUpdates() {
        updateDifferentIpRequests();
        updateUniqueIpRequests();
        updateUrlRequests(getStatisticForLastConnection().getURI());
    }

    public synchronized String creatureReport() {

        // do updates before start creature Report String
        doUpdates();

        String head = "<html><head><center><font size=15>Server statistic data</font></center></head>";

        //- общее количество запросов  - количество соединений, открытых в данный момент
        // Creature HTML table with column: "Total connection", "Unique connection", "Open connection".
        StringBuilder table = new StringBuilder();
        table.append("<table border = 2> <tbody> <tr><th>Total connection</th><th>Unique connection ")
                .append(" </th><th>Open connection</th> </tr><tr><th>")
                .append(totalConnections).append("</th><th>").append(uniqueIpRequests.size())
                .append("</th><th>").append(openConnection).append("</th></tr></tbody></table>");
        String table1 = table.toString();

        //   - счетчик запросов на каждый IP в виде таблицы с колонкам и IP, кол-во запросов, время последнего запроса
        // Creature HTML table with column: "IP", "Number request", "Last connecting time".
        table.setLength(0);
        table.append("<table border=2><tbody><tr><th>IP</th><th>Number request</th><th>Last connecting time</th></tr>");
        Date date =  new Date();
        for (Map.Entry<String, AtomicLong[]> entry : differentIpRequests.entrySet()) {
            date.setTime(entry.getValue()[1].get());
            table.append("<tr><th>").append(entry.getKey()).append("</th><th>")
                    .append(entry.getValue()[0]).append("</th><th>").append(date).append("</th></tr>");


        }
        table.append("</tbody></table>");
        String table2 = table.toString();

        //- количество уникальных запросов (по одному на IP)
        // Creature HTML table with column: "IP", "Number unique request".
        table.setLength(0);
        table.append("<table border = 2><tbody><tr><th> IP </th><th> Number unique request </th></tr>");
        for (Map.Entry<String, HashSet<String>> entry : uniqueIpRequests.entrySet()) {
            table.append("<tr><th>").append(entry.getKey()).append("</th><th>").append(entry.getValue().size())
                    .append("</th></tr>");
        }
        table.append("</tbody></table>");
        String table3 = table.toString();

        //  - количество переадресаций по url'ам  в виде таблицы, с колонками url, кол-во переадресация
        // Creature HTML table with column: "URI", "Request number".
        table.setLength(0);
        table.append("<table border = 1><tbody><tr><th> URI </th><th> Request number </th></th>");
        for (Map.Entry<String, AtomicInteger> entry : urlRequests.entrySet()) {
            table.append("<tr><th>").append(entry.getKey()).append("</th><th>").append(entry.getValue())
                    .append("</th></tr>");
        }
        table.append("</tbody></table>");
        String table4 = table.toString();


        // - в виде таблицы лог из 16 последних обработанных соединений, колонки:  src_ip, URI, timestamp,  sent_bytes,
        // received_bytes, speed (bytes/sec)
        // Creature HTML table with column: "IP", "URI", "Timestamp", "Sent bytes","Received bytes", "Speed".
        table.setLength(0);
        table.append("<table border = 1><tbody><tr><th>IP</th><th>URI</th><th>Timestamp</th><th>Sent bytes</th>")
                .append("<th>Received bytes</th><th>Speed(bytes/sec)</th></tr></tbody>");
        for (StatisticForOneConnection sfoc : lastConnections) {
            table.append("<tr><th>").append(sfoc.getIP())
                    .append("</th><th>").append(sfoc.getURI())
                    .append("</th><th>").append(sfoc.getDate())
                    .append("</th><th>").append(sfoc.getWriteBytes())
                    .append("</th><th>").append(sfoc.getReadBytes())
                    .append("</th><th>").append(sfoc.getSpeed()).append("</tr>");
        }
        table.append("</tbody></table></html>");
        String table5 = table.toString();

        table.setLength(0);
        table.append(head).append(table1).append(table2).append(table3).append(table4).append(table5);

        return table.toString();
    }
}

