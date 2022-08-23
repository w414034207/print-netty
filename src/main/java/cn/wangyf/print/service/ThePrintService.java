package cn.wangyf.print.service;

import cn.wangyf.print.model.PrintConfigs;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 打印服务service
 *
 * @author wangyf
 * @date 2019/1/4 15:35
 */
public class ThePrintService {
    private static Map<String, PrintService> printServiceMap;

    /**
     * 初始化打印机集合
     */
    private void initPrinters() {
        printServiceMap = new HashMap<>();
        // 查找所有的可用的打印服务
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices) {
            printServiceMap.put(printService.getName(), printService);
        }
    }

    public String getPrinters() {
        initPrinters();
        return JSONArray.fromObject(printServiceMap.keySet()).toString();
    }

    public String getDefaultPrinter() {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        return defaultService.getName();
    }

    public String printPdf(PrintConfigs userConfigs) throws IOException, PrinterException, InterruptedException {
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        //份数
        attr.add(new Copies(userConfigs.getCopies()));
        //装订
        attr.add(Finishings.STAPLE);
        if (userConfigs.isDuplex()) {
            //双面打印
            attr.add(Sides.DUPLEX);
        }
        if (printServiceMap == null) {
            initPrinters();
        }
        if (!userConfigs.isPortrait()) {
            // 横向打印
            attr.add(OrientationRequested.LANDSCAPE);
        }
        // 逐份打印
        attr.add(SheetCollate.COLLATED);

        if (printServiceMap == null) {
            initPrinters();
        }
        PrintService printer = printServiceMap.get(userConfigs.getPrinter());
        List<String> urlList = userConfigs.getUrls();
        if (printer == null) {
            throw new IOException("没有对应的打印服务");
        }
        if (CollectionUtils.isEmpty(urlList)) {
            throw new IOException("没有可打印的内容");
        }

        LinkedBlockingQueue<byte[]> documentsByte = new LinkedBlockingQueue<>();
        documentsByte = downloadAllDocument(urlList, documentsByte);
        byte[] fileByte;
        // 都下载结束，没问题，开始打印
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(printer);
        while ((fileByte = documentsByte.poll()) != null) {
            try (PDDocument document = PDDocument.load(fileByte)) {
                printerJob.setPageable(new PDFPageable(document));
                printerJob.print(attr);
            }
        }
        return "success";
    }

    /**
     * 下载多个待打印的文件
     *
     * @param urlList 文件地址
     * @param queue   保存下载后文件的队列
     * @return 下载的文件
     */
    private LinkedBlockingQueue<byte[]> downloadAllDocument(List<String> urlList, LinkedBlockingQueue<byte[]> queue) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newCachedThreadPool(r -> new Thread(r, "t_iop_download_" + r.hashCode()));
        CountDownLatch count = new CountDownLatch(urlList.size());
        AtomicBoolean downFlag = new AtomicBoolean(true);
        // 多线程下载文件，都下载成功之后再统一打印。
        for (String url : urlList) {
            if (StringUtils.isEmpty(url)) {
                count.countDown();
                continue;
            }
            executorService.submit(() -> {
                try {
                    queue.add(download(url));
                } catch (Exception e) {
                    // 下载失败
                    downFlag.set(false);
                } finally {
                    count.countDown();
                }
            });
        }
        executorService.shutdown();
        // 等待计数器计数
        count.await();
        long endTime = System.currentTimeMillis();
        System.out.println("下载结束，耗时：" + (endTime - startTime));
        // 如果下载失败，则清空队列
        if (!downFlag.get()) {
            throw new IOException("获取文件失败");
        }
        return queue;
    }

    private static byte[] download(String url) {
        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream baos = null;
        InputStream i = null;
        try {
            url = StringUtils.trim(url);
            URL u = new URL(url);
            i = u.openStream();
            byte[] b = new byte[1024 * 1024];
            int len;
            baos = new ByteArrayOutputStream();
            while ((len = i.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (i != null) {
                    i.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("url:" + url + "\n下载单个文件耗时：" + (endTime - startTime));
        if (baos == null) {
            return null;
        }
        return baos.toByteArray();
    }

}
