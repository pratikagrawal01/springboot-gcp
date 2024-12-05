package com.costco.gcp.pubsub.service;

import org.springframework.stereotype.Service;

@Service
public interface PubSubMessageListenerService{

	public void pullMessages();

}
