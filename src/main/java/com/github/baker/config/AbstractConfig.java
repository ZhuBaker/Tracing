package com.github.baker.config;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import com.github.baker.TracingMetaInfo;
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
 * @time: 2018年09月15日
 * @modifytime:
 */
public abstract class AbstractConfig {

	private String SERVICE_NAME ;
	private String ZIPKIN_V2_URL ;

	private Tracing tracing;

	public AbstractConfig(String SERVICE_NAME, String ZIPKIN_V2_URL) {
		this.SERVICE_NAME = SERVICE_NAME;
		this.ZIPKIN_V2_URL = ZIPKIN_V2_URL;
		this.tracing = getTracing(SERVICE_NAME,ZIPKIN_V2_URL);
	}

	/**
	 * 构建Tracing
	 * @param serviceName
	 * @param zipkinV2Url
	 * @return
	 */
	private Tracing getTracing(String serviceName ,String zipkinV2Url) {
		tracing = Tracing.newBuilder()
				.localServiceName(serviceName)
				.spanReporter(spanReporter(zipkinV2Url))
				.sampler(Sampler.ALWAYS_SAMPLE)
				.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "user-name"))
				.build();
		return tracing;
	}

	/**
	 * 构建
	 * @param zipkinUrl
	 * @return
	 */
	protected  AsyncReporter<Span> spanReporter(String zipkinUrl) {
		Sender sender = getSender(zipkinUrl);
		AsyncReporter asyncReporter = AsyncReporter.builder(sender)
				.closeTimeout(500, TimeUnit.MILLISECONDS)
				.build(SpanBytesEncoder.JSON_V2);
		return asyncReporter;
	}

	public Tracing tracing() {
		return tracing;
	}

	public abstract Sender getSender(String zipkinV2Url);


}
