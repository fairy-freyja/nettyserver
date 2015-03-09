import hamsterServer.Initializer;
import hamsterServer.StatisticForOneConnection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * Created by Wyvern on 08.03.2015.
 */
public class MetricsTest {

//    private StatisticForOneConnection oneConnectionStat = new StatisticForOneConnection();
//    //private static StatisticData statisticData;
//
//    private long startTime;
//
//    public Metrics(String ip) {
//        super(1000);
//        oneConnectionStat.setIp(ip);
//    }
//
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
//        System.out.println("IN channelActive Metrics");
//
//        startTime = System.currentTimeMillis();
//        System.out.println("trafficCounter.lastTime = " + trafficCounter.lastTime());
//        System.out.println("trafficCounter.lastCumulativeTime = " + trafficCounter.lastCumulativeTime());
//
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
//        System.out.println("IN channelRead Metrics");
//
//        if(msg instanceof HttpRequest)
//            oneConnectionStat.setUri(((HttpRequest) msg).getUri());
//
//
//    }
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        super.channelReadComplete(ctx);
//        System.out.println("IN channelReadComplete Metrics");
//
//        System.out.println("trafficCounter().cumulativeReadBytes() = " + trafficCounter().cumulativeReadBytes());
//        System.out.println("tc.lastReadBytes()); = " + trafficCounter.lastReadBytes());
//
//
//        System.out.println("tc().currentWrittenBytes() = "  + trafficCounter().cumulativeWrittenBytes());
//
//        System.out.println("trafficCounter.lastWriteThroughput()); = " + trafficCounter.lastWriteThroughput());
//
//
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("IN channelInactive Metrics");
//        System.out.println("trafficCounter().cumulativeReadBytes() = " + trafficCounter().cumulativeReadBytes());
//        oneConnectionStat.setReadBytes(trafficCounter.cumulativeReadBytes());
//        System.out.println("tc.lastReadBytes()); = " + trafficCounter.lastReadBytes());
//
//
//        oneConnectionStat.setWriteBytes(trafficCounter.cumulativeWrittenBytes());
//        System.out.println("tc().currentWrittenBytes() = "  + trafficCounter().cumulativeWrittenBytes());
//
//        oneConnectionStat.setSpeed(trafficCounter.lastWriteThroughput());
//        System.out.println("trafficCounter.lastWriteThroughput()); = " + trafficCounter.lastWriteThroughput());
//        System.out.println("trafficCounter.getRealWriteThroughput(); = " + trafficCounter.getRealWriteThroughput());
//
//        System.out.println("trafficCounter.lastTime = " + trafficCounter.lastTime());
//        System.out.println("trafficCounter.lastCumulativeTime = " + trafficCounter.lastCumulativeTime());
//        System.out.println("sec = " + (System.currentTimeMillis()-startTime)/1000);
//        long tottalEorkTime = (System.currentTimeMillis()-startTime)/1000;
//        oneConnectionStat.setSpeed((trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes()) / tottalEorkTime);
//
//        Initializer.statisticData.addLastConnection(oneConnectionStat);
//        super.channelInactive(ctx);
//
//
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//    @Override
//    public void read(ChannelHandlerContext ctx) {
//        super.read(ctx);
//        System.out.println("IN read Metrics");
//        System.out.println("trafficCounter.lastWriteThroughput()); = " + trafficCounter.lastWriteThroughput());
//        System.out.println("trafficCounter.getRealWriteThroughput(); = " + trafficCounter.getRealWriteThroughput());
//
//
//    }


}

