package hamsterServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
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
            FullHttpResponse response = registerRequest(oneConnection, uri);

            oneConnection.setUri(uri);

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

    private FullHttpResponse registerRequest(StatisticForOneConnection oneConnection, String uri) throws InterruptedException {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);

        statisticData.registerRequest(decoder.path(), oneConnection);
        switch (decoder.path()) {
            case "/hello":
                return helloWorldResponse();
            case "/redirect":
                Map<String, List<String>> params = decoder.parameters();
                if (params.containsKey("url"))
                {
                    final String redirectUri = params.get("url").get(0);
                    statisticData.registerRedirectionRequest(redirectUri);
                    return redirectResponse(redirectUri);
                }
                else
                    return invalidRedirectResponse();
            case "/status":
                return statusResponse();
            default:
                return notFoundResponse();
        }
    }


    //The method which provides an answer to the Hello world query
    private FullHttpResponse helloWorldResponse() throws InterruptedException {
        Thread.sleep(10000);
        return makeHttpResponse(wrapResponseString("Hello world!"));
    }

    //The method which provides an answer to the Redirect query
    private FullHttpResponse redirectResponse(String url) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(HttpHeaders.Names.LOCATION, url);
        return response;
    }

    private FullHttpResponse invalidRedirectResponse() {
        return makeHttpResponse(wrapResponseString("Redirect should have uri query parameter"), BAD_REQUEST);
    }

    //The method which provides an answer to the Status query
    private FullHttpResponse statusResponse() {
        return makeHttpResponse(createReport(statisticData));
    }

    //The method which provides an answer to the not found page query
    private FullHttpResponse notFoundResponse() {
        return makeHttpResponse(wrapResponseString("Invalid URI"), NOT_FOUND);
    }

    private DefaultFullHttpResponse makeHttpResponse(String text) {
        return new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(text, UTF_8));
    }

    private String wrapResponseString(String text) {
        return String.format("<head><font size=5>%s</font></head>", text);
    }

    private DefaultFullHttpResponse makeHttpResponse(String text, HttpResponseStatus status) {
        return new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(text, UTF_8));
    }

    public synchronized String createReport(StatisticData statisticData) {

        StatSnapshot s = statisticData.snapshot();


        //- общее количество запросов  - количество соединений, открытых в данный момент
        // Creature HTML table with column: "Total connection", "Unique connection", "Open connection".
        StringBuilder res = new StringBuilder();

        res.append("<html><head><center><font size=15>Server statistic data</font></center></head>")
                .append("<table border = 2> <tbody> <tr><th>Total connection</th><th>Unique connection ")
                .append(" </th><th>Open connection</th> </tr><tr><th>")
                .append(s.getTotalConnectionCount())
                .append("</th><th>")
                .append(s.getUniqueVisitorCount())
                .append("</th><th>")
                .append(s.getOpenConnectionCount())
                .append("</th></tr></tbody></table>");

        //   - счетчик запросов на каждый IP в виде таблицы с колонкам и IP, кол-во запросов, время последнего запроса
        // Creature HTML table with column: "IP", "Request number", "Last request time".
        res.append("<table border=2><tbody><tr><th>IP</th><th>Request number</th><th>Last request time</th></tr>");
        Date date = new Date();
        for (Map.Entry<String, RemoteIpInfo> entry : s.getIpInfoMap().entrySet()) {
            String ip = entry.getKey();
            RemoteIpInfo rii = entry.getValue();
            date.setTime(rii.getLastQueryTime());
            res.append("<tr><th>")
                    .append(ip)
                    .append("</th><th>")
                    .append(rii.getRequestCount())
                    .append("</th><th>")
                    .append(date)
                    .append("</th></tr>");
        }
        res.append("</tbody></table>");

        //- количество уникальных запросов (по одному на IP)
        // Creature HTML table with column: "IP", "Number unique request".
        res.append("<table border = 2><tbody><tr><th>IP</th><th>Unique request number</th></tr>");
        for (Map.Entry<String, RemoteIpInfo> entry : s.getIpInfoMap().entrySet()) {
            final String ip = entry.getKey();
            final RemoteIpInfo rii = entry.getValue();
            res.append("<tr><th>")
                    .append(ip)
                    .append("</th><th>")
                    .append(rii.getUriRequested().size())
                    .append("</th></tr>");
        }
        res.append("</tbody></table>");

        //  - количество переадресаций по url'ам  в виде таблицы, с колонками url, кол-во переадресация
        // Creature HTML table with column: "URI", "Request number".
        res.append("<table border = 1><tbody><tr><th> URI </th><th> Redirection number </th></th>");
        for (Map.Entry<String, Long> entry : s.getRedirections().entrySet()) {
            final String redirectionUri = entry.getKey();
            final Long count = entry.getValue();
            res.append("<tr><th>")
                    .append(redirectionUri)
                    .append("</th><th>")
                    .append(count)
                    .append("</th></tr>");
        }
        res.append("</tbody></table>");

        // - в виде таблицы лог из 16 последних обработанных соединений, колонки:  src_ip, URI, timestamp,  sent_bytes,
        // received_bytes, speed (bytes/sec)
        // Creature HTML table with column: "IP", "URI", "Timestamp", "Sent bytes","Received bytes", "Speed".
        res.append("<table border = 1><tbody><tr><th>IP</th><th>URI</th><th>Timestamp</th><th>Sent bytes</th>")
                .append("<th>Received bytes</th><th>Speed(bytes/sec)</th></tr></tbody>");
        for (StatisticForOneConnection sfoc : s.getLastConnections()) {
            if((sfoc.getURI() == null || sfoc.getURI().equals(""))
                    && sfoc.getReadBytes() == 0
                    && sfoc.getWriteBytes() == 0
                    && sfoc.getSpeed() == 0){
                res.append("<tr><th>").append(sfoc.getIP())
                        .append("</th><th>").append("data in processing")
                        .append("</th><th>").append(sfoc.getDate())
                        .append("</th><th>").append("data in processing")
                        .append("</th><th>").append("data in processing")
                        .append("</th><th>").append("data in processing").append("</tr>");
            }else {
                res.append("<tr><th>").append(sfoc.getIP())
                        .append("</th><th>").append(sfoc.getURI())
                        .append("</th><th>").append(sfoc.getDate())
                        .append("</th><th>").append(sfoc.getWriteBytes())
                        .append("</th><th>").append(sfoc.getReadBytes())
                        .append("</th><th>").append(sfoc.getSpeed()).append("</tr>");
            }
        }
        res.append("</tbody></table></html>");

        return res.toString();
    }

}
