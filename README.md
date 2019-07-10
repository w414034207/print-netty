# print-netty
## 打印控件-浏览器客户端调用本地打印机打印文档
### 描述
1. 用netty实现的http服务；
2. 可以通过js调用本地打印服务；
3. 用于实现浏览器客户端调用本地打印机打印文档的功能；
4. 可一次请求打印多个文件，使用线程池同时下载多个文件；
5. IE8及以上、Chrome、Firefox都测试通过。
### 实际使用
1. 使用maven打成jar包；
2. 使用打包工具将jar打包成exe文件，将32位的1.8以上版本的jre也打包进去；
3. 将打包的可执行文件放到项目中，路径配置到printService.js文件的downloadUrl，用户可通过路径下载打印控件；
4. 用户下载运行，即可使用网页提供的打印服务；
#### 另外
测试时可以不打包exe，使用batch或shell命令执行jar文件。batch示例如下：
```
rem print-netty-1.0-SNAPSHOT.jar文件和jre1.8.0_201文件夹在同一路径下；命令也在该路径下执行
jre1.8.0_201\bin\java -Dfile.encoding=utf-8 -Xms5m -Xmx200m -cp print-netty-1.0-SNAPSHOT.jar cn.wangyf.print.main.PrintMain
```
面对普通用户的时候，建议使用exe文件的形式。
### 示例
1. 下载本项目之后，直接用浏览器打开src/test/demo/index.html文件；
2. 按提示下载，并解压zip文件，运行exe；
3. 刷新页面即可看到打印机列表，选择打印机进行打印。
