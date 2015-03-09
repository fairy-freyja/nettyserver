package hamsterServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/*
 * Created by Fairy on 06.03.2015.
 */
public class Metrics extends ChannelTrafficShapingHandler {

    private StatisticForOneConnection oneConnectionStat = new StatisticForOneConnection();

    private long startTime;

    public Metrics(String ip) {
        super(1000);
        oneConnectionStat.setIp(ip);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("IN handAdd");
        Initializer.statisticData.incrementTotalRequests();
        Initializer.statisticData.incrementOpenConnection();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("IN handRem");
        Initializer.statisticData.decrementOpenConnection();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("IN channelActive Metrics");
        startTime = System.currentTimeMillis();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("IN channelRead Metrics");

        if (msg instanceof HttpRequest) {
            oneConnectionStat.setUri(((HttpRequest) msg).getUri());
//            Initializer.statisticData.updateUrlRequests(((HttpRequest) msg).getUri());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        System.out.println("IN channelReadComplete Metrics");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("IN channelInactive Metrics");

        oneConnectionStat.setReadBytes(trafficCounter.cumulativeReadBytes());

        oneConnectionStat.setWriteBytes(trafficCounter.cumulativeWrittenBytes());

        long totalWorkTime = (System.currentTimeMillis() - startTime) / 1000;
        long totalReedWrightBytes = (trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes());

        // if work time < 0 sec, set statistic report Speed value of totalReedWrightBytes;
        oneConnectionStat.setSpeed(totalWorkTime > 0 ? totalReedWrightBytes / totalWorkTime : totalReedWrightBytes);
        Initializer.statisticData.addLastConnection(oneConnectionStat);
        Initializer.statisticData.doUpdates();
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

