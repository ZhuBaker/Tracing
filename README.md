# Dubbo + Zipkin + Brave + Kafka 实现全链路追踪
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

基于Kafka 0.10.+ 以上配置：
```bash
java -DKAFKA_BOOTSTRAP_SERVERS=172.16.6.95:9092,172.16.6.95:9093,172.16.6.95:9094 -DKAFKA_TOPIC=zipkin -jar zipkin.jar  --logging.level.zipkin2=DEBUG
```
# zipkin 数据存储及其他配置

## Environment Variables
zipkin-server is a drop-in replacement for the [scala query service](https://github.com/openzipkin/zipkin/tree/scala/zipkin-query-service).

[yaml configuration](src/main/resources/zipkin-server-shared.yml) binds the following environment variables from zipkin-scala:

* `QUERY_PORT`: Listen port for the http api and web ui; Defaults to 9411
* `QUERY_ENABLED`: `false` disables the query api and UI assets. Search
may also be disabled for the storage backend if it is not needed;
Defaults to true
* `SEARCH_ENABLED`: `false` disables trace search requests on the storage
backend. Does not disable trace by ID or dependency queries. Disable this
when you use another service (such as logs) to find trace IDs;
Defaults to true
* `QUERY_LOG_LEVEL`: Log level written to the console; Defaults to INFO
* `QUERY_LOOKBACK`: How many milliseconds queries can look back from endTs; Defaults to 24 hours (two daily buckets: one for today and one for yesterday)
* `STORAGE_TYPE`: SpanStore implementation: one of `mem`, `mysql`, `cassandra`, `elasticsearch`
* `COLLECTOR_SAMPLE_RATE`: Percentage of traces to retain, defaults to always sample (1.0).

### Cassandra Storage
Zipkin's [Cassandra storage component](../zipkin-storage/cassandra)
supports version 3.11+ and applies when `STORAGE_TYPE` is set to `cassandra3`:

    * `CASSANDRA_KEYSPACE`: The keyspace to use. Defaults to "zipkin2"
    * `CASSANDRA_CONTACT_POINTS`: Comma separated list of host addresses part of Cassandra cluster. You can also specify a custom port with 'host:port'. Defaults to localhost on port 9042.
    * `CASSANDRA_LOCAL_DC`: Name of the datacenter that will be considered "local" for latency load balancing. When unset, load-balancing is round-robin.
    * `CASSANDRA_ENSURE_SCHEMA`: Ensuring cassandra has the latest schema. If enabled tries to execute scripts in the classpath prefixed with `cassandra-schema-cql3`. Defaults to true
    * `CASSANDRA_USERNAME` and `CASSANDRA_PASSWORD`: Cassandra authentication. Will throw an exception on startup if authentication fails. No default
    * `CASSANDRA_USE_SSL`: Requires `javax.net.ssl.trustStore` and `javax.net.ssl.trustStorePassword`, defaults to false.

The following are tuning parameters which may not concern all users:

    * `CASSANDRA_MAX_CONNECTIONS`: Max pooled connections per datacenter-local host. Defaults to 8
    * `CASSANDRA_INDEX_CACHE_MAX`: Maximum trace index metadata entries to cache. Zero disables caching. Defaults to 100000.
    * `CASSANDRA_INDEX_CACHE_TTL`: How many seconds to cache index metadata about a trace. Defaults to 60.
    * `CASSANDRA_INDEX_FETCH_MULTIPLIER`: How many more index rows to fetch than the user-supplied query limit. Defaults to 3.

Example usage with logging:

```bash
$ STORAGE_TYPE=cassandra3 java -jar zipkin.jar --logging.level.zipkin=trace --logging.level.zipkin2=trace --logging.level.com.datastax.driver.core=debug
```

### Elasticsearch Storage
Zipkin's [Elasticsearch storage component](../zipkin-storage/elasticsearch)
supports versions 2-6.x and applies when `STORAGE_TYPE` is set to `elasticsearch`

The following apply when `STORAGE_TYPE` is set to `elasticsearch`:

    * `ES_HOSTS`: A comma separated list of elasticsearch base urls to connect to ex. http://host:9200.
                  Defaults to "http://localhost:9200".
    * `ES_PIPELINE`: Only valid when the destination is Elasticsearch 5+. Indicates the ingest
                     pipeline used before spans are indexed. No default.
    * `ES_TIMEOUT`: Controls the connect, read and write socket timeouts (in milliseconds) for
                    Elasticsearch Api. Defaults to 10000 (10 seconds)
    * `ES_MAX_REQUESTS`: Only valid when the transport is http. Sets maximum in-flight requests from
                         this process to any Elasticsearch host. Defaults to 64.
    * `ES_INDEX`: The index prefix to use when generating daily index names. Defaults to zipkin.
    * `ES_DATE_SEPARATOR`: The date separator to use when generating daily index names. Defaults to '-'.
    * `ES_INDEX_SHARDS`: The number of shards to split the index into. Each shard and its replicas
                         are assigned to a machine in the cluster. Increasing the number of shards
                         and machines in the cluster will improve read and write performance. Number
                         of shards cannot be changed for existing indices, but new daily indices
                         will pick up changes to the setting. Defaults to 5.
    * `ES_INDEX_REPLICAS`: The number of replica copies of each shard in the index. Each shard and
                           its replicas are assigned to a machine in the cluster. Increasing the
                           number of replicas and machines in the cluster will improve read
                           performance, but not write performance. Number of replicas can be changed
                           for existing indices. Defaults to 1. It is highly discouraged to set this
                           to 0 as it would mean a machine failure results in data loss.
    * `ES_USERNAME` and `ES_PASSWORD`: Elasticsearch basic authentication, which defaults to empty string.
                                       Use when X-Pack security (formerly Shield) is in place.
    * `ES_HTTP_LOGGING`: When set, controls the volume of HTTP logging of the Elasticsearch Api.
                         Options are BASIC, HEADERS, BODY
Example usage:

To connect normally:
```bash
$ STORAGE_TYPE=elasticsearch ES_HOSTS=http://myhost:9200 java -jar zipkin.jar
```

To log Elasticsearch api requests:
```bash
$ STORAGE_TYPE=elasticsearch ES_HTTP_LOGGING=BASIC java -jar zipkin.jar
```

### Legacy (v1) storage components
The following components are no longer encouraged, but exist to help aid
transition to supported ones. These are indicated as "v1" as they use
data layouts based on Zipkin's V1 Thrift model, as opposed to the
simpler v2 data model currently used.

#### MySQL Storage
The following apply when `STORAGE_TYPE` is set to `mysql`:

    * `MYSQL_DB`: The database to use. Defaults to "zipkin".
    * `MYSQL_USER` and `MYSQL_PASS`: MySQL authentication, which defaults to empty string.
    * `MYSQL_HOST`: Defaults to localhost
    * `MYSQL_TCP_PORT`: Defaults to 3306
    * `MYSQL_MAX_CONNECTIONS`: Maximum concurrent connections, defaults to 10
    * `MYSQL_USE_SSL`: Requires `javax.net.ssl.trustStore` and `javax.net.ssl.trustStorePassword`, defaults to false.

Example usage:

```bash
$ STORAGE_TYPE=mysql MYSQL_USER=root java -jar zipkin.jar
```

### Cassandra Storage
Zipkin's [Legacy (v1) Cassandra storage component](../zipkin-storage/cassandra-v1)
supports version 2.2+ and applies when `STORAGE_TYPE` is set to `cassandra`:

The environment variables are the same as `STORAGE_TYPE=cassandra3`,
except the default keyspace name is "zipkin".

Example usage:

```bash
$ STORAGE_TYPE=cassandra java -jar zipkin.jar
```
参考： [zipkin-server模块](https://github.com/openzipkin/zipkin/tree/master/zipkin-server)



> 待扩展项
> * 抽象数据传输（扩展Kafka数据传输）支持HTTP/Kafka数据传输 - 已完成
> * Web扩展
> * 调用返回值数据打印
> * 更灵活的配置方式