package com.costco.gcp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.model.webhook.WebHook;
import com.costco.gcp.pubsub.service.PubSubMessageListenerService;
import com.costco.gcp.pubsub.service.PubSubMessagePublish;
import com.costco.gcp.service.BrandfolderService;
import com.costco.gcp.service.GcpApiService;
import com.costco.gcp.service.GcpFileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class BrandfolderController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BrandfolderService brandfolderService;

	@Autowired
	GcpApiService gcpApiService;

	@Autowired
	PubSubMessageListenerService pubSubMessageService;

	@Autowired 
	PubSubMessagePublish pubSubMessagePublish;
	
	@Autowired
	GcpFileStorageService gcpFileStorageService;

	@GetMapping({ "/hi/{name}"}) 
	public ResponseEntity<String> test (@PathVariable String name) {
		return ResponseEntity.ok("HI "+name);
	}

	@GetMapping({ "/item/{site_itemNumber}"}) 
	public JsonNode getItemNumberResponse(@PathVariable String site_itemNumber) {
		return gcpApiService.getGCPItemProductData(site_itemNumber,CommonConstants.GCP_ITEM_DIRECTORY);
	}

	@GetMapping({ "/product/{site_productNumber}"}) 
	public JsonNode getProductNumberResponse(@PathVariable String site_productNumber) {
		return gcpApiService.getGCPItemProductData(site_productNumber,CommonConstants.GCP_PRODUCT_DIRECTORY);
	}
	
	@GetMapping({ "/hero/product/{site_productNumber}"}) 
	public ResponseEntity<byte[]>  getProductHeroImage(@PathVariable String site_productNumber) {
		try {
			return gcpApiService.getHeroImage(site_productNumber,CommonConstants.GCP_PRODUCT_DIRECTORY);
		} catch (Exception e) {
			logger.error("Error Fetching hero image {}, {}",site_productNumber,e);
			return null;
		}
	}
	
	@GetMapping({ "/hero/item/{site_itemNumber}"}) 
	public ResponseEntity<byte[]> getItemHeroImage(@PathVariable String site_itemNumber) {
		try {
			return gcpApiService.getHeroImage(site_itemNumber,CommonConstants.GCP_ITEM_DIRECTORY);
		} catch (Exception e) {
			logger.error("Error Fetching hero image {} , {} ",site_itemNumber,e);
			return null;
		}
	}


	@GetMapping({ "/pubsub/process"}) 
	public ResponseEntity<String> webhookListner() {
		try {
			pubSubMessageService.pullMessages();
		} catch (Exception e) {
			logger.error("Error on message polling {}",e);
			return ResponseEntity.internalServerError().body("Message polling failed.");
		}
		return ResponseEntity.ok("Message received");
	}

	@PostMapping({"/pubsub/publish"})
	public ResponseEntity<String> publish(@RequestBody String requestBody) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			logger.info("Webhook received :");
			WebHook webHookData = objectMapper.readValue(requestBody, WebHook.class);
			logger.info("webHook id {} asset {}",webHookData.data().webhookId(),webHookData.data().attributes().bfKey());
		}catch(Exception ex) {
			logger.error("Incorrect webhook payload : {}", requestBody);
			return ResponseEntity.badRequest().body("Incorrect webhook payload.");
		}
		return pubSubMessagePublish.publishMessages(requestBody);
	}
}
