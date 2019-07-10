package cn.wangyf.print.netty;

import cn.wangyf.print.constants.PrintConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

/**
 * Http服务
 *
 * @author wangyf
 * @date 2019/6/15 15:48
 */
public class HttpServer {
    private final static int PORT = PrintConstants.SERVER_PORT;

    public static void start() {
        final HttpHandler httpHandler = new HttpHandler();
        // 创建EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 创建EventLoopGroup
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                //指定所使用的NIO传输Channel
                .channel(NioServerSocketChannel.class)
                //使用指定的端口设置套接字地址
                .localAddress(new InetSocketAddress(PORT))
                // 添加一个EchoServerHandler到Channle的ChannelPipeline
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    /**
                     * 初始化channel
                     */
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        // 获取管道
                        socketChannel.pipeline()
                                // 解码
                                .addLast(new HttpRequestDecoder())
                                // 编码
                                .addLast(new HttpResponseEncoder())
                                /* aggregator，消息聚合器（重要）。
                                Netty4中为什么能有FullHttpRequest这个东西，
                                就是因为有他，HttpObjectAggregator，如果没有他，
                                就不会有那个消息是FullHttpRequest的那段Channel，
                                同样也不会有FullHttpResponse，HttpObjectAggregator(512 * 1024)的参数含义是消息合并的数据大小，
                                如此代表聚合的消息内容长度不超过512kb。*/
                                .addLast(new HttpObjectAggregator(512 * 1024))
                                //EchoServerHandler被标注为@shareable,所以我们可以总是使用同样的案例
                                .addLast(httpHandler); // 请求的业务类
                    }
                });

        try {
            // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅的关闭EventLoopGroup，释放所有的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
