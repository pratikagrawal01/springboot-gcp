package com.costco.gcp.pubsub.service;

import java.util.Collection;

import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;

public interface BrandfolderAssetSyncService {

	public void processMessages(Collection<AcknowledgeablePubsubMessage> messages);

}
