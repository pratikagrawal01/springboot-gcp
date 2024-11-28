package com.costco.gcp.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface GcpApiService {
	
	public JsonNode getItemData(String site_itemNumber);
	
	public JsonNode getProductData(String site_productNumber);
	
}
