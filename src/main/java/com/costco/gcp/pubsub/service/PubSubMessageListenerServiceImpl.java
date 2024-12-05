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

	@Autowired
	private PubSubTemplate pubSubTemplate;
	
	@Autowired
	private BrandfolderAssetSyncService brandfolderAssetSyncService;

	// Method to pull messages from Pub/Sub
	public void pullMessages() {
		Collection<AcknowledgeablePubsubMessage> messages = pubSubTemplate.pull(pubsubSubscription, 10, true); 
		logger.info("total message receive: {}",messages.size());
		brandfolderAssetSyncService.processMessages(messages);
	}

}


/*
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("pubsubInputChannel") MessageChannel inputChannel, PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, pubsubSubscription);
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);
		return adapter;
	}

	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver() {
		return message -> {
			logger.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
			BasicAcknowledgeablePubsubMessage originalMessage =
					message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			originalMessage.ack();
		};
	}
}*/
