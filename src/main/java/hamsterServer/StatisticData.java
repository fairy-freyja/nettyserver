package hamsterServer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fairy on 08.03.15.
 */
public class StatisticData {

    // String is IP, and Long[0] for number query; Long[1] - for  last query time in milliseconds
    private HashMap<String, AtomicLong[]> differentIpRequests;


    //Hear Key String contains IP, Value String[] - unique requests,
    // So number unique requests for some IP is String[].length for current IP Kay String
    // - количество уникальных запросов (по одному на IP)
    private HashMap<String, HashSet<String>> uniqueIpRequests;

    // Hear Kay String contains URL name, and Value Integer is number of this url call
    private Map<String, Integer> urlRequests = new TreeMap<>();


    //  лог из 16 последних обработанных соединений, колонки:
    // src_ip, URI, timestamp, sent_bytes, received_bytes, speed(bytes/sec)
    // statistic for last 16 connections
    private LinkedList<StatisticForOneConnection> lastConnections;


// - количество соединений, открытых в данный момент
    // current time open connection
    private AtomicInteger openConnection; // =    private long numberActive = 0;


    //    - общее количество запросов
    private final AtomicInteger totalConnections; // =   private long numberQuery = 0;


    public  StatisticData(){
        differentIpRequests = new HashMap<>();
        lastConnections = new LinkedList<>();
        openConnection = new AtomicInteger(0);
        totalConnections = new AtomicInteger(-1);
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
        if(lastConnections.size() > 16){
            lastConnections.remove(0);
        }
        lastConnections.add(oneConnection);

    }

    public void incrementTotalRequests() {
        totalConnections.incrementAndGet();
    }
    public void incrementOpenConnection (){
        openConnection.incrementAndGet();
    }
    public void decrementOpenConnection(){
        openConnection.decrementAndGet();
    }

    // check url and add or update its value of number request
    public void updateUrlRequests(String url){
        if(urlRequests.containsKey(url)){
            urlRequests.put(url, urlRequests.get(url)+1);
        }else {
            urlRequests.put(url, 1);
        }

    }

    // in differentIpRequests Kay String is IP, and Value AtomicLong[] contains query number
    // and last query time in milliseconds
    // This method update query number and last query time if  differentIpRequests contains last connection IP
    // And add a new pair of values if it's not.
    public void updateDifferentIpRequests(){

        String ip = getStatisticForLastConnection().getIP();
        if ( differentIpRequests.containsKey(ip)){
            AtomicLong[] value = differentIpRequests.get(ip);
            value[0].incrementAndGet();
            value[1] = new AtomicLong(lastConnections.get(lastConnections.size()-1).getDate().getTime());
            differentIpRequests.put(ip, value);
        } else {
            AtomicLong numberRequests = new AtomicLong(1);
            AtomicLong time =  new AtomicLong(lastConnections.get(lastConnections.size()-1).getDate().getTime());
            AtomicLong[] value = {numberRequests, time};
            differentIpRequests.put(ip,value);
        }
    }

    //Hear Key String contains IP, Value String[] - unique requests,
    // So number unique requests for some IP is String[].length for current IP Kay String
    // - количество уникальных запросов (по одному на IP)
    public void updateUniqueIpRequests(){
        String ip = getStatisticForLastConnection().getIP();
        String uri = getStatisticForLastConnection().getURI();
        HashSet<String> currentValue = new HashSet<>();

        if(uniqueIpRequests.containsKey(ip)){
            currentValue = uniqueIpRequests.get(ip);
            if(currentValue.contains(uri)) {
                return;
            }
        }
        currentValue.add(uri);
        uniqueIpRequests.put(ip, currentValue);

    }


    public synchronized void doUpdates(){
        updateDifferentIpRequests();
        updateUniqueIpRequests();
        updateUrlRequests(getStatisticForLastConnection().getURI());
    }

	public synchronized String creatureReport() {

        // do updates before start creature Report String
        doUpdates();

        String hello = "<html><head><center><font size=15>Server statistic data</font></center></head>";

        //   - общее количество запросов  - количество соединений, открытых в данный момент
        String table1 = "<table border = 2> <tbody> <tr><th>Total connection</th><th>Unique connection "  +
                " </th><th>Open connection</th> </tr><tr><th>"
                + totalConnections + "</th><th>" + uniqueIpRequests.size() + "</th><th>" + openConnection + "</th></tr></tbody></table>";

        //   - счетчик запросов на каждый IP в виде таблицы с колонкам и IP, кол-во запросов, время последнего запроса
        String table2 = "<table border = 2><tbody><tr><th> IP </th><th> Number request </th><th> Last connecting time </th></tr>";
        for (Map.Entry<String, AtomicLong[]> entry : differentIpRequests.entrySet()) {
            table2 += "<tr><th> " + entry.getKey() + " </th><th> " + entry.getValue()[0] + " </th><th> " + entry.getValue()[1] + " </th></tr>";
        }
        table2 += "</tbody></table>";


        //- количество уникальных запросов (по одному на IP)
        String table3 = "<table border = 2><tbody><tr><th> IP </th><th> Number unique request </th></tr>";
        for (Map.Entry<String, HashSet<String>> entry : uniqueIpRequests.entrySet()) {
            table3 +=  "<tr><th> " + entry.getKey() + " </th><th> " + entry.getValue().size() + " </th></tr>";
        }
        table3 += "</tbody></table>";


        //  - количество переадресаций по url'ам  в виде таблицы, с колонками url, кол-во переадресация
		String table4 = "<table border = 1><tbody><tr><th> URL </th><th> Request Number </th></th>";
        for (Map.Entry<String, Integer> entry : urlRequests.entrySet()) {
            table4 += "<tr><th> " + entry.getKey() + " </th><th> " + entry.getValue() + " </th></tr>";
        }
        table4 += "</tbody></table>";

        // - в виде таблицы лог из 16 последних обработанных соединений, колонки:  src_ip, URI, timestamp,  sent_bytes, received_bytes, speed (bytes/sec)
        String table5 = "<table border = 1><tbody><tr><th>IP</th><th>URI</th><th>Timestamp</th><th>Sent bytes</th><th>Recieved bytes</th><th>Speed(bytes/sec)</th></tr></tbody>";
		for(StatisticForOneConnection sfoc: lastConnections) {
				table5 +="<tr><th>" + sfoc.getIP() +
                        "</th><th>" + sfoc.getURI() +
                        "</th><th>" + sfoc.getDateString() +
                        "</th><th>" + sfoc.getWriteBytes() +
                        "</th><th>" + sfoc.getReadBytes() +
                        "</th><th>" + sfoc.getSpeed() + "</tr>";
		}
		table5 = table5 + "</tbody></table></html>";

		return hello + table1 + table2 + table3 + table4 + table5;
	}
}



// TODO
//        serverStatus.append("</tr></table><th valign=top><table border=3><tr><th colspan=2>Redirected to</th></tr>")
//        .append("<tr><th>Link</th><th>Times</th></tr>");
//        for (Map.Entry<String, AtomicInteger> link: redirectCounter.entrySet()) {
//        serverStatus.append("<tr><th>").append(link.getKey())
//        .append("</th><th>").append(link.getValue())
//        .append("</th></tr>");
//        }
//        serverStatus.append("</table></th></tr></table></center></body></html>");
//        return serverStatus.toString();