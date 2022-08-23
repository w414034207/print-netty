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
    /** 打印机 */
    private String printer;
    /** 打印份数 */
    private int copies;
    /** 双面打印 */
    private boolean duplex;
    /** 纵向打印 */
    private boolean portrait;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(String origin, String urlStr) {
        String[] urlArr = urlStr.split(";");
        urls = new ArrayList<>();
        for (String url : urlArr) {
            url = StringUtils.trim(url);
            if (StringUtils.isNotEmpty(url)) {
                // 如果url不是以http(s)://开头，并且origin不为空的，认为是相对路径，增加origin
                if (StringUtils.isNotEmpty(origin) && !url.startsWith("http://") && !url.startsWith("https://")) {
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

    public boolean isPortrait() {
        return portrait;
    }

    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
    }
}
