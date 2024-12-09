package com.costco.gcp.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface GcpApiService {
	
	public JsonNode getGCPItemProductData(String site_productNumber,String folderDirectory);
	
}
