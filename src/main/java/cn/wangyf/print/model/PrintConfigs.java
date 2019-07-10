package cn.wangyf.print.model;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 打印配置参数
 *
 * @author wangyf
 * @date 2019/1/3 15:25
 */
public class PrintConfigs {
    private List<String> urls;
    private String printer;
    private int copies;
    private boolean duplex;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(String origin, String urlStr) {
        String[] urlArr = urlStr.split(";");
        urls = new ArrayList<>();
        for (String url : urlArr) {
            url = StringUtils.trim(url);
            if (StringUtils.isNotEmpty(url)) {
                if (StringUtils.isNotEmpty(origin)) {
                    url = origin + url;
                }
                urls.add(url);
            }
        }
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public boolean isDuplex() {
        return duplex;
    }

    public void setDuplex(boolean duplex) {
        this.duplex = duplex;
    }
}
