package com.costco.gcp.pubsub.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PubSubMessagePublish{

	public ResponseEntity<String> publishMessages(String jsonNode);

}
