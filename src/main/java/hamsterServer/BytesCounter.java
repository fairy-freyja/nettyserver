package hamsterServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/*
 * Created by Fairy on 06.03.2015.
 */
public class BytesCounter extends ChannelTrafficShapingHandler {

    private StatisticData statisticData;
    private long startTime;
    private String ip;

    public BytesCounter(StatisticData statisticData, String ip) {
        super(1000);
        this.statisticData = statisticData;
        this.ip = ip;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("IN handAdd");

        statisticData.incrementTotalRequests();
        statisticData.incrementOpenConnection();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("IN handRem");
        statisticData.decrementOpenConnection();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("IN channelActive Metrics");
        StatisticForOneConnection oneConnection = new StatisticForOneConnection();
        oneConnection.setIp(ip);
        ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).set(oneConnection);
        statisticData.updateDifferentIpRequests(ctx);
        startTime = System.currentTimeMillis();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        StatisticForOneConnection oneConnection = ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).get();
        oneConnection.setReadBytes(trafficCounter.cumulativeReadBytes());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("IN channelInactive Metrics");

        StatisticForOneConnection oneConnection = ctx.channel().attr(StatisticData.CURRENT_CONNECTION_INFO_KEY).get();
        long writeByte = trafficCounter.cumulativeWrittenBytes();
        oneConnection.setWriteBytes(writeByte);

        long totalWorkTime = (System.currentTimeMillis() - startTime) / 1000;
        long totalReedWrightBytes = (trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes());

        // if work time < 0 sec, set statistic report Speed value of totalReedWrightBytes;
        oneConnection.setSpeed(totalWorkTime > 0 ? totalReedWrightBytes / totalWorkTime : totalReedWrightBytes);
        statisticData.addLastConnection(oneConnection);

        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

