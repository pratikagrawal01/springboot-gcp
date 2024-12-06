package com.costco.gcp.pubsub.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.service.BrandfolderAssetSyncService;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;

@Service
public class PubSubMessageListenerServiceImpl implements PubSubMessageListenerService,CommonConstants {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.cloud.gcp.pubsub.subscription-name}")
	private String pubsubSubscription;
	
	@Value("${pubsub.process.message.limit}")
	private Integer messageProcessLimit;

	@Autowired
	private PubSubTemplate pubSubTemplate;
	
	@Autowired
	private BrandfolderAssetSyncService brandfolderAssetSyncService;

	// Method to pull messages from Pub/Sub
	public void pullMessages() {
		Collection<AcknowledgeablePubsubMessage> messages = pubSubTemplate.pull(pubsubSubscription, messageProcessLimit, true); 
		logger.info("total pub/sub message received for processing: {}",messages.size());
		brandfolderAssetSyncService.processMessages(messages);
	}

}