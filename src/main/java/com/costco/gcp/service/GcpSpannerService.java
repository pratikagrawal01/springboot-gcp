package com.costco.gcp.service;

import java.util.Map;
import java.util.Optional;

import com.costco.gcp.model.AssetData;
import com.google.gson.JsonObject;

public interface GcpSpannerService{

	public void insertItemAssetData(Map<String, Map<String, JsonObject>> assetMap);

	public Optional<AssetData> fetchAsset(String assetKey);

	public void deleteAsset(String assetKey);

}
