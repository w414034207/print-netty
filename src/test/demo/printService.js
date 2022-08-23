const printServerAddress = "http://127.0.0.1:31777/";
const downloadUrl = "print.zip";

function getPrinters(callbackFunc) {
    $.ajax({
        type: "GET",
        url: printServerAddress + "getPrinters?_m=" + Math.random(),
        success: function (msg) {
            let dataObj = eval("(" + msg + ")");
            callbackFunc(dataObj);
        },
        error: downloadIoPrint
    });
}

function getDefaultPrinter(callbackFunc) {
    $.ajax({
        type: "GET",
        url: printServerAddress + "getDefaultPrinter?_m=" + Math.random(),
        success: callbackFunc,
        error: downloadIoPrint
    });
}

function printPdf(url, config) {
    $.ajax({
        type: "POST",
        data: {
            'printer': config.printer
            , 'copies': config.copies
            , 'duplex': config.duplex
            , 'url': url
            , 'portrait': config.portrait
        },
        url: printServerAddress + "printPdf?_m=" + Math.random(),
        success: config.done
    });
}

function downloadIoPrint() {
    if (confirm("请下载解压并运行打印服务后重试！点击【确定】下载。")) {
        location.href = downloadUrl;
    }
}