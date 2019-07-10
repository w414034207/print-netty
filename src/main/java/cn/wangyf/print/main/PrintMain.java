package cn.wangyf.print.main;

import cn.wangyf.print.netty.HttpServer;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * 主程序入口
 *
 * @author wangyf
 * @date 2019/1/4 15:30
 */
public class PrintMain {
    static {
        // outToFile();
    }

    public static void main(String[] args) {
        HttpServer.start();
    }

    private static void outToFile() {
        try {
            PrintStream print = new PrintStream("consoleMsg.log");
            System.setOut(print);
            System.setErr(print);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
