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

import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;


/*
 * Created by Fairy on 06.03.2015.
 */
public class MainServerHandler extends ChannelInboundHandlerAdapter {

    MainServerHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if ((msg instanceof HttpRequest)) {
            String uri = ((HttpRequest) msg).getUri();
            FullHttpResponse response = checkURI((uri));
            // TODO delete string
            System.out.println("responseLength = " + response.content().writerIndex());
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
        return new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(Initializer.statisticData.creatureReport(), UTF_8));
    }

    //The method which provides an answer to the not found page query
    private FullHttpResponse notFoundValue() {
        return new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
    }

}
