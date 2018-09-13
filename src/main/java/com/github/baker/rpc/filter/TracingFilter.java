package com.github.baker.rpc.filter;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.github.baker.SingletonTracingFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: bakerZhu
 * @description :
 * @time: 2018年09月09日
 * @modifytime:
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class TracingFilter  implements Filter {

	private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);

	private static Tracing tracing;
	private static Tracer tracer;
	private static TraceContext.Extractor<Map<String, String>> extractor;
	private static TraceContext.Injector<Map<String, String>> injector;

	static final Propagation.Getter<Map<String, String>, String> GETTER =
			new Propagation.Getter<Map<String, String>, String>() {
				@Override
				public String get(Map<String, String> carrier, String key) {
					return carrier.get(key);
				}

				@Override
				public String toString() {
					return "Map::get";
				}
			};
	static final Propagation.Setter<Map<String, String>, String> SETTER =
			new Propagation.Setter<Map<String, String>, String>() {
				@Override
				public void put(Map<String, String> carrier, String key, String value) {
					carrier.put(key, value);
				}

				@Override
				public String toString() {
					return "Map::set";
				}
			};

	static {
		tracing = SingletonTracingFactory.getTracing();
		tracer = tracing.tracer();
		extractor = tracing.propagation().extractor(GETTER);
		injector = tracing.propagation().injector(SETTER);
	}

	public TracingFilter() {
		super();
	}

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		RpcContext rpcContext = RpcContext.getContext();
		Span.Kind kind = rpcContext.isProviderSide() ? Span.Kind.SERVER : Span.Kind.CLIENT;
		final Span span;
		if (kind.equals(Span.Kind.CLIENT)) {
			span = tracer.nextSpan();
			injector.inject(span.context(), invocation.getAttachments());
		} else {
			TraceContextOrSamplingFlags extracted = extractor.extract(invocation.getAttachments());
			span = extracted.context() != null ? tracer.joinSpan(extracted.context()) : tracer.nextSpan(extracted);
		}

		if (!span.isNoop()) {
			span.kind(kind).start();
			String service = invoker.getInterface().getSimpleName();
			String method = RpcUtils.getMethodName(invocation);
			span.kind(kind);
			span.name(service + "/" + method);
			InetSocketAddress remoteAddress = rpcContext.getRemoteAddress();
			span.remoteIpAndPort(
					remoteAddress.getAddress() != null ? remoteAddress.getAddress().getHostAddress() : remoteAddress.getHostName(),remoteAddress.getPort());
		}

		boolean isOneway = false, deferFinish = false;
		try (Tracer.SpanInScope scope = tracer.withSpanInScope(span)){
			collectArguments(invocation, span, kind);
			Result result = invoker.invoke(invocation);

			if (result.hasException()) {
				onError(result.getException(), span);
			}
			isOneway = RpcUtils.isOneway(invoker.getUrl(), invocation);

			Future<Object> future = rpcContext.getFuture();
			if (future instanceof FutureAdapter) {
				deferFinish = true;
				((FutureAdapter) future).getFuture().setCallback(new FinishSpanCallback(span));
			}
			return result;
		} catch (Error | RuntimeException e) {
			onError(e, span);
			throw e;
		} finally {
			if (isOneway) {
				span.flush();
			} else if (!deferFinish) {
				span.finish();
			}
		}
	}

	static void onError(Throwable error, Span span) {
		span.error(error);
		if (error instanceof RpcException) {
			span.tag("dubbo.error_msg", RpcExceptionEnum.getMsgByCode(((RpcException) error).getCode()));
		}
	}

	static void collectArguments(Invocation invocation, Span span, Span.Kind kind) {
		if (kind == Span.Kind.CLIENT) {
			StringBuilder fqcn = new StringBuilder();
			Object[] args = invocation.getArguments();
			if (args != null && args.length > 0) {
				try {
					fqcn.append(JSON.json(args));
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
				}
			}
			span.tag("args", fqcn.toString());
		}
	}



	static final class FinishSpanCallback implements ResponseCallback {
		final Span span;

		FinishSpanCallback(Span span) {
			this.span = span;
		}

		@Override
		public void done(Object response) {
			span.finish();
		}

		@Override
		public void caught(Throwable exception) {
			onError(exception, span);
			span.finish();
		}
	}

	private enum RpcExceptionEnum {
		UNKNOWN_EXCEPTION(0, "unknown exception"),
		NETWORK_EXCEPTION(1, "network exception"),
		TIMEOUT_EXCEPTION(2, "timeout exception"),
		BIZ_EXCEPTION(3, "biz exception"),
		FORBIDDEN_EXCEPTION(4, "forbidden exception"),
		SERIALIZATION_EXCEPTION(5, "serialization exception"),;

		private int code;

		private String msg;

		RpcExceptionEnum(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public static String getMsgByCode(int code) {
			for (RpcExceptionEnum error : RpcExceptionEnum.values()) {
				if (code == error.code) {
					return error.msg;
				}
			}
			return null;
		}
	}

}

