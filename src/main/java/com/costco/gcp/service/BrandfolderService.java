package com.costco.gcp.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface BrandfolderService {
	
	public JsonNode getItemNumberResponse(String site_itemNumber);
	
	public JsonNode getItemNumberResponse(String site_itemNumber, boolean updateDB);
	
	public JsonNode getProductNumberResponse(String site_productNumber);

	public JsonNode getProductNumberResponse(String site_productNumber, boolean updateDB);
	
	public JsonNode getAssetData(String assetKey);

}
