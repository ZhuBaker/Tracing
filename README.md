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
## 变量默认值(不进行设置则按如下配置)
| Key | Default Value | Description | 
| - | :-: | - | 
| transport_type | http | 数据传输方式,支持 http/kafka 两种 | 
| zipkin_host | localhost:9411 | 传输目的地：<br/>传输方式为http时,为zipkin地址 . <br/> 传输方式为kafka时,为 kafka地址,broker之间以逗号间隔 .  | 
| service_name | trace-default | 项目(节点)标识 | 
| kafka_topic | zipkin | 传输方式为kafka时的topic | 


资源目录根路径下添加tracing.properties文件
![tracing.properties](http://wx1.sinaimg.cn/mw1024/006QW2Smgy1fv83pwskp1j30kv092wev.jpg "tracing.properties")


一次调用信息
![tracing.properties](http://wx4.sinaimg.cn/mw1024/006QW2Smgy1fv84r2zuvzj30mg0hvmy1.jpg "tracing.properties")
调用链
![tracing.properties](http://wx2.sinaimg.cn/mw1024/006QW2Smgy1fv84r30py1j30xk0bsgm7.jpg "tracing.properties")
调用成功失败汇总
![tracing.properties](http://wx1.sinaimg.cn/mw1024/006QW2Smgy1fv84r35vhjj31130bk3z4.jpg "tracing.properties")

调用链：
![tracing.properties](http://wx3.sinaimg.cn/mw1024/006QW2Smgy1fv84ge9genj31f80egwff.jpg "tracing.properties")


## 整合Kafka 
1.搭建Kafka运行环境 Scala
2.搭建并启动Kafka
3.启动zipkin:启动zipkin时 建议先看官方文档,鉴于kafka版本更新较快,zipkin连接kafka时不同版本之间的差异, 建议先看[GitHub](https://github.com/openzipkin/zipkin/tree/master/zipkin-server) 后进行zipkin的启动
针对不同的kafka版本 zipkin的启动配置方式不同 , 基于Kafka 0.10.+ 以上版本重要配置做下说明:

| Attribute | Property | Description | 
| - | :-: | - | 
| KAFKA_BOOTSTRAP_SERVERS | bootstrap.servers | Comma-separated list of brokers, ex. 127.0.0.1:9092. No default | 
| KAFKA_GROUP_ID | group.id | The consumer group this process is consuming on behalf of. Defaults to zipkin  | 
| KAFKA_TOPIC | N/A | Comma-separated list of topics that zipkin spans will be consumed from. Defaults to zipkin | 
| KAFKA_STREAMS | N/A | Count of threads consuming the topic. Defaults to 1 | 


### Overriding other properties
You may need to override [other consumer properties](https://kafka.apache.org/082/documentation.html#consumerconfigs) than what zipkin
explicitly defines. In such case, you need to prefix that property name
with "zipkin.collector.kafka.overrides" and pass it as a CLI argument or
system property.

For example, to override "overrides.auto.offset.reset", you can set a
prefixed system property:

```bash
$ KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092 java -Dzipkin.collector.kafka.overrides.auto.offset.reset=largest -jar zipkin.jar
```
进行Kafka中属性覆盖  例如，要覆盖auto.offset.reset，可以设置名为的系统属性 zipkin.collector.kafka.overrides.auto.offset.reset

## Logging  日志打印级别控制

By default, zipkin writes log messages to the console at INFO level and above. You can adjust categories using the `--logging.level.XXX` parameter, a `-Dlogging.level.XXX` system property, or by adjusting [yaml configuration](src/main/resources/zipkin-server-shared.yml).

For example, if you want to enable debug logging for all zipkin categories, you can start the server like so:

```bash
$ java -jar zipkin.jar --logging.level.zipkin2=DEBUG
```
参考：
* [ZipKin配置](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)
* [ZipKin整合Kafka配置:kafka10](https://github.com/openzipkin/zipkin/blob/master/zipkin-collector/kafka/README.md)
* [ZipKin整合Kafka默认配置:kafka10](https://github.com/openzipkin/zipkin/blob/master/zipkin-autoconfigure/collector-kafka/README.md)
* [ZipKin整合Kafka配置:kafka08](https://github.com/openzipkin/zipkin/blob/master/zipkin-collector/kafka08/README.md)
* [ZipKin整合Kafka默认配置:kafka08](https://github.com/openzipkin/zipkin/blob/master/zipkin-autoconfigure/collector-kafka08/README.md)

例：
```bash
java -DKAFKA_BOOTSTRAP_SERVERS=172.16.6.95:9092,172.16.6.95:9093,172.16.6.95:9094 -DKAFKA_TOPIC=zipkin -jar zipkin.jar  --logging.level.zipkin2=DEBUG
```


> 待扩展项
> * 抽象数据传输（扩展Kafka数据传输）支持HTTP/Kafka数据传输 - 已完成
> * 调用返回值数据打印
> * 更灵活的配置方式