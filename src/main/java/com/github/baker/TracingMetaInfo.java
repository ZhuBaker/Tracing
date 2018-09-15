package com.github.baker;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: bakerZhu
 * @description : 读取资源文件
 * @time: 2018年09月13日
 * @modifytime:
 */
public class TracingMetaInfo {

	private static Logger logger = LoggerFactory.getLogger(TracingMetaInfo.class);

	private static final String RESOURCE_FILE_NAME = "tracing.properties";
	private static final String DEFAULT_ZIPKIN_V2_URL = "http://localhost:9411/api/v2/spans";
	private static final String DEFAULT_SERVICE_NAME = "tracer-default";
	private static final String DEFAULT_TRANSPORT_TYPE = "http";
	private static final String DEFAULT_KAFKA_TOPIC = "zipkin";

	private static Properties prop = null;
	public static String ZIPKIN_V2_URL = null;
	public static String SERVICE_NAME = null;
	public static String TRANSPORT_TYPE = null;
	public static String KAFKA_TOPIC = null;

	static {
		try {
			prop = new Properties();
			prop.load(TracingMetaInfo.class.getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME));
			ZIPKIN_V2_URL = setZipKin(prop.getProperty("zipkin_host") , DEFAULT_ZIPKIN_V2_URL);
			SERVICE_NAME = setProperty(prop.getProperty("service_name") , DEFAULT_SERVICE_NAME);
			TRANSPORT_TYPE = setProperty(prop.getProperty("transport_type"),DEFAULT_SERVICE_NAME);
			TRANSPORT_TYPE = setProperty(prop.getProperty("kafka_topic"),DEFAULT_KAFKA_TOPIC);
		} catch (FileNotFoundException e) {
			logger.warn(" please add "+RESOURCE_FILE_NAME+" file at resource root directory.");
			ZIPKIN_V2_URL = setZipKin(null , DEFAULT_ZIPKIN_V2_URL);
			SERVICE_NAME = setProperty(null , DEFAULT_SERVICE_NAME);
			TRANSPORT_TYPE = setProperty(null , DEFAULT_TRANSPORT_TYPE);
			TRANSPORT_TYPE = setProperty(prop.getProperty("topic"),DEFAULT_KAFKA_TOPIC);
		} catch (IOException e) {
			logger.warn(" loading "+RESOURCE_FILE_NAME+" file error.");
			ZIPKIN_V2_URL = setZipKin(null , DEFAULT_ZIPKIN_V2_URL);
			SERVICE_NAME = setProperty(null , DEFAULT_SERVICE_NAME);
			TRANSPORT_TYPE = setProperty(null , DEFAULT_TRANSPORT_TYPE);
			TRANSPORT_TYPE = setProperty(null , DEFAULT_KAFKA_TOPIC);
		}
	}

	private static String setZipKin(String value , String defaultValue) {
		if(value != null && value.trim().length() > 0) {
			return "http://"+value.trim()+"/api/v2/spans";
		}
		return defaultValue;
	}

	private static String setProperty(String value , String defaultValue) {
		return value != null && value.trim().length() > 0 ? value.trim() : defaultValue;
	}

}
