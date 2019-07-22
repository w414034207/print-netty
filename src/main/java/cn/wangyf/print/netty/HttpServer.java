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
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                //指定所使用的NIO传输Channel
                .channel(NioServerSocketChannel.class)
                //使用指定的端口设置套接字地址
                .localAddress(new InetSocketAddress(PORT))
                // 添加Handler到Channle的ChannelPipeline
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        // 获取管道
                        socketChannel.pipeline()
                                // 解码
                                .addLast(new HttpRequestDecoder())
                                // 编码
                                .addLast(new HttpResponseEncoder())
                                /* aggregator，消息聚合器 */
                                .addLast(new HttpObjectAggregator(512 * 1024))
                                // HttpHandler被标注为@shareable,所以我们可以总是使用同样的案例
                                .addLast(httpHandler);
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
            // 关闭EventLoopGroup，释放所有的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
