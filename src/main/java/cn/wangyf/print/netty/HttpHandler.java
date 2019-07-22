package cn.wangyf.print.netty;

import cn.wangyf.print.controller.PrintController;
import cn.wangyf.print.model.HttpRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理Http请求
 *
 * @author wangyf
 * @date 2019/6/15 15:44
 */
@ChannelHandler.Sharable
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        DefaultFullHttpResponse response = executeRequest(msg);

        HttpHeaders heads = response.headers();
        // 返回内容的MIME类型
        heads.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
        // 响应体的长度
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 允许跨域访问
        heads.add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        // 响应给客户端
        ctx.write(response);
    }

    /**
     * 数据发送完毕，则关闭连接通道.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    /**
     * 发生异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (null != cause) {
            cause.printStackTrace();
        }
        if (null != ctx) {
            ctx.close();
        }
    }

    /**
     * 处理请求，返回应答
     *
     * @param msg 请求信息
     * @return 应答信息
     * @throws UnsupportedEncodingException
     */
    private DefaultFullHttpResponse executeRequest(FullHttpRequest msg) throws UnsupportedEncodingException {
        HttpResponseStatus responseStatus = HttpResponseStatus.OK;
        String returnMsg = "";
        try {
            HttpRequest httpRequest = new HttpRequest(msg);
            Class<PrintController> printControllerClass = PrintController.class;
            Method invokeMethod = printControllerClass.getMethod(httpRequest.getMethod(), HttpRequest.class);
            PrintController printController = PrintController.getInstance();
            returnMsg = (String) invokeMethod.invoke(printController, httpRequest);
        } catch (NoSuchMethodException e) {
            responseStatus = HttpResponseStatus.NOT_FOUND;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus,
                Unpooled.wrappedBuffer(returnMsg.getBytes()));
    }

}
