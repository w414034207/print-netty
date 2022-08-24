package cn.wangyf.print.controller;


import cn.wangyf.print.model.HttpRequest;
import cn.wangyf.print.model.PrintConfigs;
import cn.wangyf.print.service.ThePrintService;

/**
 * 控制Controller
 *
 * @author wangyf
 * @date 2019/1/3 12:02
 */
public class PrintController {
    private static PrintController selfInstance = new PrintController();
    private static ThePrintService thePrintService = new ThePrintService();

    private PrintController() {
    }

    public static PrintController getInstance() {
        return selfInstance;
    }

    public String getPrinters(HttpRequest request) {
        return thePrintService.getPrinters();
    }

    public String getDefaultPrinter(HttpRequest request) {
        return thePrintService.getDefaultPrinter();
    }

    public String printPdf(HttpRequest request) {
        try {
            PrintConfigs config = new PrintConfigs();
            config.setCopies(Integer.parseInt(request.getParameter().get("copies")));
            config.setDuplex(Boolean.parseBoolean(request.getParameter().get("duplex")));
            config.setPortrait(Boolean.parseBoolean(request.getParameter().get("portrait")));
            config.setPrinter(request.getParameter().get("printer"));
            config.setUrls(request.getOrigin(), request.getParameter().get("url"));
            return thePrintService.printPdf(config);
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
