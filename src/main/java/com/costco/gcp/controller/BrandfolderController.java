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
		//pubSubMessageService.pullMessages();
		//	brandfolderRepository.save(new Brandfolder(UUID.randomUUID().toString(),testJson));
		return ResponseEntity.ok("HI "+name);
	}

	@GetMapping({ "/item/{site_itemNumber}"}) 
	public JsonNode getItemNumberResponse(@PathVariable String site_itemNumber) {
		return gcpApiService.getItemData(site_itemNumber);
	}

	@GetMapping({ "/product/{site_productNumber}"}) 
	public JsonNode getProductNumberResponse(@PathVariable String site_productNumber) {
		return gcpApiService.getProductData(site_productNumber);
	}

	@GetMapping({ "/webhook"}) 
	public ResponseEntity<String> webhookListner() {
		try {
			pubSubMessageService.pullMessages();
		} catch (Exception e) {
			logger.info("Error on message polling {}",e);
		}
		return ResponseEntity.ok("Message received");
	}

	@PostMapping({"/publish"})
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


	@GetMapping({"/testing"})
	public ResponseEntity<String> test () throws JsonMappingException, JsonProcessingException {
		String message = "{\r\n"
				+ "  \"data\": {\r\n"
				+ "    \"attributes\": {\r\n"
				+ "      \"key\": \"kqhhffhfjkjq95nsfh48xm9h\",\r\n"
				+ "      \"event_time\": \"2024-11-14 17:18:45.000000\",\r\n"
				+ "      \"event_type\": \"asset.update\",\r\n"
				+ "      \"brandfolder_key\": \"x99bv7m9qxwvtcm8txg8r37\",\r\n"
				+ "      \"organization_key\": \"qd9l41-bsuawo-cavoo0\"\r\n"
				+ "    },\r\n"
				+ "    \"webhook_id\": \"c968e2e3-f341-4f9e-ac79-d54d689df994\"\r\n"
				+ "  }\r\n"
				+ "}";
		ObjectMapper objectMapper = new ObjectMapper();
		WebHook webHookData = objectMapper.readValue(message, WebHook.class);
		logger.info("webHook id {} asset {}",webHookData.data().webhookId(),webHookData.data().attributes().bfKey());
		return ResponseEntity.ok("HI ");
	}

}
