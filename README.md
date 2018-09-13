# Dubbo + Zipkin + Brave实现全链路追踪
分布式跟踪系统还有其他比较成熟的实现，例如：Naver的Pinpoint、Apache的HTrace、阿里的鹰眼Tracing、京东的Hydra、新浪的Watchman，美团点评的CAT，skywalking等。
本次主要利用Dubbo数据传播特性扩展Filter接口来实现链路追踪的目的

重点和难点主要是zipkin及brave使用及特性,当前brave版本为 5.2.0 为 2018年8月份发布的稳定版 , zipkin版本为2.2.1 所需JDK为1.8

## 快速启动zipkin
下载最新的zipkin并启动
```java
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
java -jar zipkin.jar
```
输入  http://localhost:9411/zipkin/
进入WebUI界面如下
![zipUI](http://wx2.sinaimg.cn/mw1024/006QW2Smgy1fv83cycuubj31gg0edjs9.jpg "zipUI")

---------------

> 处理项
> * Dubbo sync async oneway 调用处理
> * RPC异常处理
> * 普通业务异常处理

---------------

> 测试项
> * Dubbo sync async oneway 测试
> * RPC异常测试
> * 普通业务异常测试
> * 并发测试

---------------

## 配置方式
POM依赖添加
```xml
<dependency>
    <groupId>com.github.baker</groupId>
    <artifactId>Tracing</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
资源目录根路径下添加tracing.properties文件
![tracing.properties](http://wx1.sinaimg.cn/mw1024/006QW2Smgy1fv83pwskp1j30kv092wev.jpg "tracing.properties")
一次调用信息
![tracing.properties](http://wx4.sinaimg.cn/mw1024/006QW2Smgy1fv84r2zuvzj30mg0hvmy1.jpg "tracing.properties")
调用链
![tracing.properties](http://wx2.sinaimg.cn/mw1024/006QW2Smgy1fv84r30py1j30xk0bsgm7.jpg "tracing.properties")
调用成功失败汇总
![tracing.properties](http://wx1.sinaimg.cn/mw1024/006QW2Smgy1fv84r35vhjj31130bk3z4.jpg "tracing.properties")
zipkinHost 指定zipkin服务器IP:PORT 默认为localhost:9411
serviceName 指定应用名称  默认为trace-default

调用链：
![tracing.properties](http://wx3.sinaimg.cn/mw1024/006QW2Smgy1fv84ge9genj31f80egwff.jpg "tracing.properties")

> 待扩展项
> * 抽象数据传输（扩展Kafka数据传输）
> * 调用返回值数据打印
> * 更灵活的配置方式
