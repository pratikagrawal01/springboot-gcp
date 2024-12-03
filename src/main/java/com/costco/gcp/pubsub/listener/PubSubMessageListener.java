package com.costco.gcp.pubsub.listener;

import org.springframework.stereotype.Service;

@Service
public interface PubSubMessageListener{

	public void pullMessages();

}
