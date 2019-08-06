package cn.wangyf.print.model;

import cn.wangyf.print.constants.PrintConstants;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
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
        HttpMethod method = msg.method();
        this.requestMethod = StringUtils.upperCase(method.name());
        this.uri = msg.uri();

        QueryStringDecoder uriDecoder = new QueryStringDecoder(uri);
        this.method = uriDecoder.path().substring(1);

        if (method.equals(HttpMethod.GET)) {
            parameter = convertParam(uriDecoder.parameters());
        } else if (method.equals(HttpMethod.POST)) {
            // 请求内容
            ByteBuf byteBuf = msg.content();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String requestStr = new String(bytes, PrintConstants.DEFAULT_ENCODING);

            parameter = new HashMap<>();
            String contentType = StringUtils.lowerCase(msg.headers().get(HttpHeaderNames.CONTENT_TYPE));
            if (StringUtils.startsWith(contentType, HttpHeaderValues.APPLICATION_JSON.toString())) {
                JSONObject obj = JSONObject.fromObject(requestStr);
                for (Object key : obj.keySet()) {
                    parameter.put(key.toString(), obj.get(key).toString());
                }
            } else if (StringUtils.startsWith(contentType, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
                QueryStringDecoder contentDecoder = new QueryStringDecoder(requestStr, false);
                parameter = convertParam(contentDecoder.parameters());
            }

            this.origin = Optional.ofNullable(msg.headers().get(HttpHeaderNames.ORIGIN))
                    .filter(str -> !StringUtils.equals(str, "null")).orElse("");
        }
    }

    /**
     * 解析参数
     *
     * @param paramStr 字符串
     * @return 参数键值对
     */
    private static Map<String, String> convertParam(Map<String, List<String>> paramStr) {
        Map<String, String> param = new HashMap<>(20);
        for (Map.Entry<String, List<String>> attr : paramStr.entrySet()) {
            for (String attrVal : attr.getValue()) {
                param.put(attr.getKey(), attrVal);
            }
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
