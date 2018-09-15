package com.github.baker;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.github.baker.config.AbstractConfig;
import com.github.baker.config.KafkaTracingConfig;
import com.github.baker.config.OkHttpTracingConfig;
import com.github.baker.enums.TransportEnum;
import com.github.baker.rpc.filter.TracingFilter;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: bakerZhu
 * @description:
 * @time: 2018年09月13日
 * @modifytime:
 */
public class SingletonTracingFactory {

	private static final Logger logger = LoggerFactory.getLogger(SingletonTracingFactory.class);

	private static AbstractConfig config;
	private static Tracing tracing;

	/**
	 * 构建tracing上下文
	 */
	private static void createInstance() {
		if(TransportEnum.KAFKA.getType().equals(TracingMetaInfo.TRANSPORT_TYPE)) {
			config = new KafkaTracingConfig(TracingMetaInfo.SERVICE_NAME, TracingMetaInfo.ZIPKIN_V2_URL, TracingMetaInfo.KAFKA_TOPIC);
		} else {
			config = new OkHttpTracingConfig(TracingMetaInfo.SERVICE_NAME, TracingMetaInfo.ZIPKIN_V2_URL);
		}
		tracing = config.tracing();
		logger.info("create tracing successful . SERVICE_NAME : " + TracingMetaInfo.SERVICE_NAME + ".  ZIPKIN_V2_URL : " + TracingMetaInfo.ZIPKIN_V2_URL);
	}

	public static Tracing getTracing() {
		if(tracing == null) {
			synchronized (SingletonTracingFactory.class) {
				if(tracing == null) {
					createInstance();
				}
			}
		}
		return tracing;
	}
}
