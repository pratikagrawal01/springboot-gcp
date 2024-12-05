package com.costco.gcp.pubsub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;

@Service
public class PubSubMessagePublishImpl implements PubSubMessagePublish,CommonConstants {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.cloud.gcp.pubsub.topic-name}")
	private String pubsubTopicId;

	@Autowired
	private PubSubTemplate pubSubTemplate;

	public ResponseEntity<String> publishMessages(String message) {
		try {
			pubSubTemplate.publish(pubsubTopicId, message.toString());
			logger.info("Message published to topic.");
			return ResponseEntity.ok("Message Received Successfully");
		} catch (Exception e) {
			logger.error("Error publishing message: {}" ,e);
			return ResponseEntity.badRequest().body("Message publishing failed");
		} 
	}
}

