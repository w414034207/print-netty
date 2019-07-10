package cn.wangyf.print.model;

import cn.wangyf.print.constants.PrintConstants;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义http请求对象
 *
 * @author wangyf
 * @date 2019/1/4 16:47
 */
public class HttpRequest {
    private String requestString;
    private String requestMethod;
    private String uri;
    private String method;
    private Map<String, String> parameter;
    private String origin;

    /**
     * 构造函数，传入netty的Http请求对象
     *
     * @param msg http请求对象
     * @throws UnsupportedEncodingException 编码异常
     */
    public HttpRequest(FullHttpRequest msg) throws UnsupportedEncodingException {
        this.requestString = msg.toString();
        this.requestMethod = StringUtils.upperCase(msg.method().name());
        this.uri = msg.uri();

        // 接收请求内容并打印
        ByteBuf byteBuf = msg.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String requestStr = new String(bytes, PrintConstants.DEFAULT_ENCODING);

        int methodIndex = uri.lastIndexOf("/");
        int paramIndex = uri.indexOf("?");
        if (paramIndex >= 0) {
            method = uri.substring(methodIndex + 1, paramIndex);
        } else {
            method = uri.substring(methodIndex + 1);
        }

        if (StringUtils.equals(requestMethod, PrintConstants.METHOD_GET)) {
            if (paramIndex >= 0) {
                String params = uri.substring(paramIndex + 1);
                parameter = convertParam(params);
            }
        } else if (StringUtils.equals(requestMethod, PrintConstants.METHOD_POST)) {
            Map<String, String> paramMap = convertParam(requestStr);
            parameter = new HashMap<>();
            paramMap.keySet().forEach((key) -> {
                try {
                    parameter.put(urlDecode(key), urlDecode(paramMap.get(key)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            this.origin = Optional.ofNullable(msg.headers().get(HttpHeaderNames.ORIGIN))
                    .filter(str -> !StringUtils.equals(str, "null")).orElse("");
        }
    }

    /**
     * url解码
     *
     * @param str 字符串
     * @return 解码后的字符串
     * @throws UnsupportedEncodingException 编码异常
     */
    private String urlDecode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(StringUtils.trim(str), PrintConstants.DEFAULT_ENCODING);
    }

    /**
     * 解析参数
     *
     * @param paramStr 字符串
     * @return 参数键值对
     */
    private static Map<String, String> convertParam(String paramStr) {
        Map<String, String> param = new HashMap<>(10);
        if (StringUtils.isEmpty(paramStr)) {
            return param;
        }
        String[] paramArr = paramStr.split("&");
        for (String paramItem : paramArr) {
            String[] paramItemArr = paramItem.split("=");
            param.put(StringUtils.trim(paramItemArr[0]), StringUtils.trim(paramItemArr[1]));
        }
        return param;
    }

    public Map<String, String> getParameter() {
        return parameter;
    }

    public String getMethod() {
        return method;
    }

    public String getOrigin() {
        return origin;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
