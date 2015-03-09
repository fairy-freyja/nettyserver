package hamsterServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;



public class Initializer extends ChannelInitializer<SocketChannel> {
    public static StatisticData statisticData = new StatisticData();

	@Override
	protected void initChannel(SocketChannel sc) throws Exception {

        String ip = sc.remoteAddress().getHostString();
        statisticData.incrementTotalRequests();

        ChannelPipeline pipeline = sc.pipeline();

		pipeline.addLast("decoder", new HttpRequestDecoder());
	    pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("metrics", new Metrics(ip));
        pipeline.addLast("handler", new MainServerHandler());
    }
}