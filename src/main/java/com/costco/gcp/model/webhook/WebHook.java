package com.costco.gcp.model.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebHook(Data data) {

	public record Data(
				Attributes attributes, 
				@JsonProperty("webhook_id") String webhookId
				) {}
	
	public record Attributes(
				@JsonProperty("key") String key,
				@JsonProperty("event_time") String eventTime,
				@JsonProperty("event_type") String eventType,
				@JsonProperty("brandfolder_key") String bfKey,
				@JsonProperty("organization_key") String orgKey
				) {}
	
}