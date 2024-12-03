package com.costco.gcp.brandfolder.api;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BrandFolderClient implements CommonConstants{

	/** The logger */
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${web.espot.bf.api.bearerToken}")
	private String bearerToken;

	@Autowired
	private CloseableHttpClient httpClient;

	/**
	 * Makes the BrandFolder GET API call.
	 * @param String key,String apiUrl,String bearerToken
	 * @return JsonNode
	 * @throws Exception 
	 */
	public JsonNode makeBFApiCal(String apiUrl) throws Exception {
		HttpGet httpGet = new HttpGet(apiUrl);
		httpGet.addHeader(AUTHORIZATION, "Bearer " + bearerToken);
		httpGet.addHeader(CONTENT_TYPE, CONTENT_JSON);

		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(response.getEntity().getContent());
				if(logger.isDebugEnabled()) {
					logger.debug("Response code: "+response.getStatusLine().getStatusCode());
					logger.debug("Response: "+jsonNode.toString());
				}
				return jsonNode;
			}
		} catch (Exception e) {
			logger.error(" Error in makeBFApiCal for {} , {}",apiUrl,e);
			throw e;
		}
		return null;
	}
}
