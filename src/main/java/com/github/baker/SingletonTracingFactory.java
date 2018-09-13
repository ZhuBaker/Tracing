package com.github.baker;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
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

	private static Tracing tracing;

	private static void createInstance() {
		//构建tracing上下文
		tracing = Tracing.newBuilder()
				.localServiceName(TracingMetaInfo.SERVICE_NAME)
				.spanReporter(spanReporter(TracingMetaInfo.ZIPKIN_V2_URL))
				.sampler(Sampler.ALWAYS_SAMPLE)
				.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "user-name"))
				.build();
		logger.info("create tracing successful . SERVICE_NAME : " + TracingMetaInfo.SERVICE_NAME + ".  ZIPKIN_V2_URL : " + TracingMetaInfo.ZIPKIN_V2_URL);
	}

	private static AsyncReporter<Span> spanReporter(String zipkinUrl) {
		Sender sender = OkHttpSender.create(zipkinUrl);
		AsyncReporter asyncReporter = AsyncReporter.builder(sender)
				.closeTimeout(500, TimeUnit.MILLISECONDS)
				.build(SpanBytesEncoder.JSON_V2);

		return asyncReporter;
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
