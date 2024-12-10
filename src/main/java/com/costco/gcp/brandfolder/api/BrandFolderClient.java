package com.costco.gcp.brandfolder.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
	
	public ResponseEntity<byte[]> getImageFromUrl(String imageUrl) throws IOException {
		HttpGet httpGet = new HttpGet(imageUrl);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			 HttpEntity entity = response.getEntity();
			 if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                 // Convert the response entity (image) to a byte array
                 try (InputStream inputStream = entity.getContent()) {
                     // Convert image InputStream to byte array
                     byte[] imageBytes = inputStream.readAllBytes();
                     
                     // Set up the response headers for the image
                     HttpHeaders headers = new HttpHeaders();
                     headers.setContentType(MediaType.IMAGE_JPEG); // Assuming JPEG format
                     headers.setContentLength(imageBytes.length);
                     
                     // Return the image as a ResponseEntity
                     return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
                 }
             } else {
                 // If the response is not OK, return 404 Not Found
                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
             }
		} catch (Exception e) {
			logger.error(" Error in makeBFApiCal for {} , {}",imageUrl,e);
			throw e;
		}
    }
}
