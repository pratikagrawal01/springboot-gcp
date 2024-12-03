package com.costco.gcp.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class HttpClientConfig {

	@Autowired
	private Environment env;
	
    @Bean
    public CloseableHttpClient closeableHttpClient() {
    	int timeout = Integer.valueOf(env.getProperty("web.brandfolder.api.timeout").toString());
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();			
		CloseableHttpClient httpClient = HttpClientBuilder
				.create()
				.disableCookieManagement()
				.setRetryHandler((exception, executionCount, context) -> {
					if (executionCount <= 3) return true;
					else return false;
				}).setDefaultRequestConfig(config) 
				.build();
        return httpClient;
    }
}
