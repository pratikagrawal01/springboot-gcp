package com.costco.gcp.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.model.AssetData;
import com.costco.gcp.repository.AssetRepository;
import com.google.gson.JsonObject;

@Service
public class GcpSpannerServiceImpl implements GcpSpannerService,CommonConstants{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	AssetRepository assetRepository;
	
	@Override
	public void insertItemAssetData(Map<String, Map<String, JsonObject>> assetMap) {
		if (logger.isDebugEnabled())
			logger.debug("Map size {} for DB insertion.", assetMap.size());
		for (Map.Entry<String, Map<String, JsonObject>> entry : assetMap.entrySet()) {
			if (logger.isDebugEnabled()) 
				logger.debug("DB update started for AssetId : {}", entry.getKey());
			AssetData assetData = new AssetData();
			assetData.setAssetId(entry.getKey());
			assetData.setSite(entry.getValue().get(BF_BUSINESSUNIT));
			assetData.setItemNumber(entry.getValue().get(BF_ITEMNUMBERS));
			assetData.setProductnumber(entry.getValue().get(BF_PRODUCTID));
			assetData.setMetaData(entry.getValue().get(BF_DATA));
			assetData.setUpdateDate(LocalDateTime.now());
			assetRepository.save(assetData);
			if (logger.isDebugEnabled()) 
				logger.debug("DB update successfull for AssetId : {}", entry.getKey());
		}
	}
	
	@Override
	public Optional<AssetData> fetchAsset(String assetKey) {
		if (logger.isDebugEnabled())
			logger.debug("Fetching data from spanner: {}", assetKey);
		
		return assetRepository.findById(assetKey);
	}


}
