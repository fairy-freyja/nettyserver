package hamsterServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;


/*
 * Created by Fairy on 06.03.2015.
 */
public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private  StatisticData statisticData;

    MainServerHandler(StatisticData statisticData) {
        this.statisticData = statisticData;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        StatisticForOneConnection oneConnection = ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).get();

        if ((msg instanceof HttpRequest)) {
            String uri = ((HttpRequest) msg).getUri();
            FullHttpResponse response = checkURI((uri));
            oneConnection.setUri(uri);
            statisticData.updateUrlRequests(oneConnection);
            statisticData.updateUniqueIpRequests(oneConnection);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught in Main");
        cause.printStackTrace();
        ctx.close();
    }

    private FullHttpResponse checkURI(String uri) throws InterruptedException {

        String url = "";
        if (uri.contains("/redirect")) {
            url = new QueryStringDecoder(uri).parameters().get("url").get(0);
            uri = "/redirect";
        }

        switch (uri) {
            case "/hello":
                return valueHelloWorld();
            case "/redirect":
                return valueRedirect(url);
            case "/status":
                return valueStatus();
            default:
                return notFoundValue();
        }
    }

    //The method which provides an answer to the Hello world query
    private FullHttpResponse valueHelloWorld() throws InterruptedException {
        String hello = "<head><font size=5>Hello world!</font></head>";
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer(hello, UTF_8));
        Thread.sleep(10000);
        return response;
    }

    //The method which provides an answer to the Redirect query
    private FullHttpResponse valueRedirect(String url) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, url);
        return response;
    }

    //The method which provides an answer to the Status query
    private FullHttpResponse valueStatus() {
        return new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(creatureReport(statisticData.snapshot()), UTF_8));
    }

    //The method which provides an answer to the not found page query
    private FullHttpResponse notFoundValue() {
        return new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
    }

    public synchronized String creatureReport(StatSnapshot s) {

        // check possibility to creature report
        if(! (s.getDifferentIpRequests().size() > 0 && s.getUniqueIpRequests().size() > 0 && s.getLastConnections().length > 0)){
            return "<html><head><center><font size=15>Server statistic data is not available</font></center></head>";
        }


        String head = "<html><head><center><font size=15>Server statistic data</font></center></head>";

        //- общее количество запросов  - количество соединений, открытых в данный момент
        // Creature HTML table with column: "Total connection", "Unique connection", "Open connection".
        StringBuilder table = new StringBuilder();
        table.append("<table border = 2> <tbody> <tr><th>Total connection</th><th>Unique connection ")
                .append(" </th><th>Open connection</th> </tr><tr><th>")
                .append(s.getTotalConnections()).append("</th><th>").append(s.getUniqueIpRequests().size())
                .append("</th><th>").append(s.getOpenConnection()).append("</th></tr></tbody></table>");
        String table1 = table.toString();

        //   - счетчик запросов на каждый IP в виде таблицы с колонкам и IP, кол-во запросов, время последнего запроса
        // Creature HTML table with column: "IP", "Number request", "Last connecting time".
        table.setLength(0);
        table.append("<table border=2><tbody><tr><th>IP</th><th>Number request</th><th>Last connecting time</th></tr>");
        Date date =  new Date();
        for (Map.Entry<String, Long[]> entry : s.getDifferentIpRequests().entrySet()) {
            date.setTime(entry.getValue()[1]);
            table.append("<tr><th>").append(entry.getKey()).append("</th><th>")
                    .append(entry.getValue()[0]).append("</th><th>").append(date).append("</th></tr>");


        }
        table.append("</tbody></table>");
        String table2 = table.toString();

        //- количество уникальных запросов (по одному на IP)
        // Creature HTML table with column: "IP", "Number unique request".
        table.setLength(0);
        table.append("<table border = 2><tbody><tr><th> IP </th><th> Number unique request </th></tr>");
        for (Map.Entry<String, HashSet<String>> entry : s.getUniqueIpRequests().entrySet()) {
            table.append("<tr><th>").append(entry.getKey()).append("</th><th>").append(entry.getValue().size())
                    .append("</th></tr>");
        }
        table.append("</tbody></table>");
        String table3 = table.toString();

        //  - количество переадресаций по url'ам  в виде таблицы, с колонками url, кол-во переадресация
        // Creature HTML table with column: "URI", "Request number".
        table.setLength(0);
        table.append("<table border = 1><tbody><tr><th> URI </th><th> Request number </th></th>");
        for (Map.Entry<String, Integer> entry : s.getUrlRequests().entrySet()) {
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
        for (StatisticForOneConnection sfoc : s.getLastConnections()) {
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
